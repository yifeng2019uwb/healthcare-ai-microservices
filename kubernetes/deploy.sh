#!/bin/bash
# kubernetes/deploy.sh — Deploy health-ai to GKE
# Usage: ./deploy.sh [all|build|infra|app|status]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
EBPF_EDR_DIR="${EBPF_EDR_DIR:-$(cd "$SCRIPT_DIR/../../../../ebpf-edr-demo" 2>/dev/null && pwd || true)}"
K8S_DIR="$SCRIPT_DIR"
PULUMI_DIR="$SCRIPT_DIR/pulumi"
NAMESPACE="health-ai"
PROJECT_ID="ebpfagent"
IMAGE_PREFIX="ghcr.io/yifeng2019uwb"
SERVICES="auth-service provider-service ai-service gateway"
ENV_FILE="$REPO_ROOT/docker/.env"

# ── logging ──────────────────────────────────────────────────────────────────
info()    { echo "[INFO]  $*"; }
success() { echo "[OK]    $*"; }
warn()    { echo "[WARN]  $*"; }
error()   { echo "[ERROR] $*"; exit 1; }

# ── load credentials from docker/.env ────────────────────────────────────────
load_env() {
    [[ -f "$ENV_FILE" ]] || error ".env not found at $ENV_FILE"
    set -a
    # shellcheck disable=SC1090
    source "$ENV_FILE"
    set +a
}

# ── build JARs + push images ──────────────────────────────────────────────────
build_images() {
    info "Building JARs (mvn)..."
    (cd "$REPO_ROOT/services" && mvn clean package -DskipTests -q)
    success "JARs built"

    for svc in $SERVICES; do
        info "Building and pushing $svc (linux/amd64)..."
        docker buildx build --platform linux/amd64 --no-cache --push \
            -t "${IMAGE_PREFIX}/${svc}:latest" \
            -f "$REPO_ROOT/services/${svc}/Dockerfile" \
            "$REPO_ROOT/services/${svc}"
        success "$svc pushed"
    done
}

# ── namespace + secrets + configmap + RLS ────────────────────────────────────
deploy_infra() {
    info "Applying namespace and service account..."
    kubectl apply -f "$K8S_DIR/namespace.yaml"

    info "Creating health-ai-secrets..."
    load_env
    if kubectl get secret health-ai-secrets -n "$NAMESPACE" >/dev/null 2>&1; then
        warn "health-ai-secrets already exists, skipping"
    else
        kubectl create secret generic health-ai-secrets \
            --from-literal=db-url="$SPRING_DATASOURCE_URL" \
            --from-literal=db-username="$SPRING_DATASOURCE_USERNAME" \
            --from-literal=db-password="$SPRING_DATASOURCE_PASSWORD" \
            --from-literal=jwt-private-key="$JWT_PRIVATE_KEY" \
            --from-literal=jwt-public-key="$JWT_PUBLIC_KEY" \
            --from-literal=gemini-api-key="$GEMINI_API_KEY" \
            -n "$NAMESPACE"
        success "health-ai-secrets created"
    fi

    info "Applying ConfigMap..."
    kubectl apply -f "$K8S_DIR/configmap.yaml"

    enable_rls

    success "Infra ready"
}

# ── enable Row Level Security on Supabase ────────────────────────────────────
enable_rls() {
    info "Enabling Row Level Security on Supabase tables..."
    load_env
    # Extract host and port from JDBC URL: jdbc:postgresql://host:port/db?params
    DB_HOST=$(echo "$SPRING_DATASOURCE_URL" | sed -E 's|jdbc:postgresql://([^:/]+).*|\1|')
    DB_PORT=$(echo "$SPRING_DATASOURCE_URL" | sed -E 's|.*://[^:]+:([0-9]+)/.*|\1|')
    RLS_SQL="$REPO_ROOT/healthcare-infra/schema/enable-rls.sql"

    PGPASSWORD="$SPRING_DATASOURCE_PASSWORD" psql \
        -h "$DB_HOST" -p "$DB_PORT" \
        -U "$SPRING_DATASOURCE_USERNAME" \
        -d postgres \
        -f "$RLS_SQL"
    success "Row Level Security enabled"
}

# ── deployments + services ────────────────────────────────────────────────────
deploy_app() {
    info "Applying Services..."
    kubectl apply -f "$K8S_DIR/service.yaml"

    info "Applying Deployments..."
    kubectl apply -f "$K8S_DIR/deployment.yaml"

    info "Waiting for deployments to be ready..."
    for svc in auth-service provider-service ai-service gateway; do
        info "  waiting: $svc"
        kubectl rollout status deployment/"$svc" -n "$NAMESPACE" --timeout=300s
        success "  $svc ready"
    done

    _apply_daemonset
}

# ── status ────────────────────────────────────────────────────────────────────
show_status() {
    echo ""
    info "=== Pods ==="
    kubectl get pods -n "$NAMESPACE"

    echo ""
    info "=== Services ==="
    kubectl get services -n "$NAMESPACE"

    echo ""
    GATEWAY_IP=$(kubectl get svc gateway -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || true)
    if [[ -n "$GATEWAY_IP" ]]; then
        success "Gateway: http://$GATEWAY_IP:8080"
        echo ""
        info "Run integration tests:"
        echo "  GATEWAY_URL=http://$GATEWAY_IP:8080 ./integration_tests/run-it.sh all"
    else
        warn "Gateway external IP pending — run: kubectl get svc gateway -n $NAMESPACE"
    fi
}

# ── eBPF EDR DaemonSet ────────────────────────────────────────────────────────
EBPF_EDR_DS_URL="https://raw.githubusercontent.com/yifeng2019uwb/ebpf-edr-demo/main/k8s/ebpf-edr-ds.yaml"
EBPF_EDR_DS="$EBPF_EDR_DIR/k8s/ebpf-edr-ds.yaml"

_apply_daemonset() {
    if [[ -z "$EBPF_EDR_DIR" ]] || [[ ! -f "$EBPF_EDR_DS" ]]; then
        warn "eBPF EDR not found at $EBPF_EDR_DS — skipping DaemonSet"
        return 0
    fi
    if grep -q "TO_BE_SET" "$EBPF_EDR_DS"; then
        warn "eBPF EDR image not configured in ebpf-edr-ds.yaml — skipping DaemonSet"
        return 0
    fi

    info "Applying eBPF EDR DaemonSet from local repo..."
    local region cluster
    region=$(cd "$PULUMI_DIR" && pulumi stack output "clusterRegions" --json 2>/dev/null | jq -r '.[0]')
    cluster=$(cd "$PULUMI_DIR" && pulumi stack output "clusterName-${region}" 2>/dev/null)

    REGION="$region" CLUSTER_NAME="$cluster" GOOGLE_CLOUD_PROJECT="$PROJECT_ID" \
        envsubst '${REGION} ${CLUSTER_NAME} ${GOOGLE_CLOUD_PROJECT}' < "$EBPF_EDR_DS" | kubectl apply -f -
    kubectl rollout restart daemonset/ebpf-edr -n kube-system
    kubectl rollout status daemonset/ebpf-edr -n kube-system --timeout=120s
    success "eBPF EDR DaemonSet deployed (region=$region)"
}

# ── get kubectl credentials from Pulumi stack ────────────────────────────────
get_credentials() {
    local region cluster zone
    region=$(cd "$PULUMI_DIR" && pulumi stack output "clusterRegions" --json 2>/dev/null | jq -r '.[0]') \
        || error "Cannot read clusterRegions — run 'pulumi up' first in kubernetes/pulumi/"
    cluster=$(cd "$PULUMI_DIR" && pulumi stack output "clusterName-${region}" 2>/dev/null)
    zone=$(cd "$PULUMI_DIR" && pulumi stack output "clusterZone-${region}" 2>/dev/null)
    gcloud container clusters get-credentials "$cluster" \
        --zone "$zone" --project "$PROJECT_ID" --quiet
    success "kubectl context set to $cluster"
}

# ── main ──────────────────────────────────────────────────────────────────────
main() {
    local cmd="${1:-all}"

    command -v kubectl >/dev/null 2>&1 || error "kubectl not found"
    command -v jq     >/dev/null 2>&1 || error "jq not found — brew install jq"
    command -v psql   >/dev/null 2>&1 || error "psql not found — brew install libpq"

    case "$cmd" in
        all)
            build_images
            get_credentials
            deploy_infra
            deploy_app
            show_status
            ;;
        build)
            build_images
            ;;
        infra)
            get_credentials
            deploy_infra
            ;;
        app)
            get_credentials
            deploy_app
            show_status
            ;;
        rls)
            enable_rls
            ;;
        status)
            get_credentials
            show_status
            ;;
        destroy)
            info "Destroying GKE cluster and all associated resources via Pulumi..."
            warn "This will delete the cluster, node pool, service account, and Artifact Registry repos."
            read -rp "Type 'yes' to confirm: " confirm
            [[ "$confirm" == "yes" ]] || { info "Aborted."; exit 0; }
            (cd "$PULUMI_DIR" && pulumi destroy)
            success "Infrastructure destroyed"
            ;;
        *)
            echo "Usage: $0 [all|build|infra|app|rls|status|destroy]"
            echo "  all     — build images, apply infra+secrets, deploy services, apply eBPF DaemonSet"
            echo "  build   — build JARs and push Docker images to Artifact Registry"
            echo "  infra   — namespace, secret, configmap, enable Supabase RLS"
            echo "  app     — deployments, services, wait rollout, eBPF DaemonSet"
            echo "  rls     — enable Row Level Security on Supabase tables (idempotent)"
            echo "  status  — show pods, services, gateway IP"
            echo "  destroy — tear down GKE cluster and all Pulumi-managed resources"
            exit 1
            ;;
    esac
}

main "$@"
