# Oracle VM Deploy Plan

> **LEGACY** — Oracle Free Tier account terminated 2026-06-06. All VMs deleted.


> **Status**: PHASE 2 COMPLETE — All 4 services deployed and running. Integration tests passing.
> Pulumi stack: `healthcare-oracle-infra / dev` — applied (6 resources: VCN, IGW, SecurityList, RouteTable, Subnet, 2× Instance)
> Next: Phase 3 — eBPF EDR on instance-2

---

## Goal

Deploy healthcare services to 2× Oracle Cloud Free Tier VMs, then install eBPF EDR agent on
the backend VM to monitor provider + AI service traffic.

## VM Layout

| VM | Hostname | IP | Services | Actual RAM (idle) |
|----|----------|----|----------|-------------------|
| instance-1 | `healthcare-gateway` | 163.192.46.25 | gateway (8080) + auth-service (8082) | ~110MB |
| instance-2 | `healthcare-backend` | 163.192.30.193 | provider-service (8083) + ai-service (8085) + **eBPF agent** | ~160MB |

Patient-service is **excluded** — not deployed in any env.

eBPF goes on **instance-2** because that's where the interesting clinical traffic is:
provider writing conditions/allergies, ai-service calling Gemini.

Region: `us-sanjose-1` · Availability domain: `HiJw:US-SANJOSE-1-AD-1`
Shape: `VM.Standard.E2.1.Micro` (1 OCPU, 1 GB RAM) · OS: Oracle Linux 9
SSH user: `opc` · SSH key: `~/.ssh/oracle_vm`

---

## Phase 1 — Code Changes ✅ COMPLETE

### Step 1 — Fix Pulumi infra code ✅

- `compute.go` — cloud-init switched from Ubuntu (`apt-get`) to Oracle Linux (`dnf`)
- `compute.go` — removed stale `/data/postgres` block (using Supabase, no local PostgreSQL)
- `compute.go` — parameter renamed from `ubuntuImageId` → `imageId`
- `main.go` — `GetImages()` auto-discovers Oracle Linux 9 image compatible with `VM.Standard.E2.1.Micro`;
  no hardcoded image OCID needed
- `setup.sh` + `env.example` — credentials helper that writes `~/.oci/config` and sets Pulumi config

### Step 2 — Deploy approach ✅

Local build + SCP (not multi-stage Dockerfiles):
1. Build all JARs locally with Maven reactor: `mvn -pl auth-service,provider-service,ai-service,gateway -am clean package -DskipTests -q`
2. SCP JARs + thin Dockerfiles to each VM
3. `docker compose up --build` on each VM

The `shared` module dependency is handled by Maven's `-am` flag (builds required modules automatically).

### Step 3 — Split Docker Compose ✅

| File | Used by | Services |
|------|---------|----------|
| `docker/docker-compose.yml` | local dev only | all 4 services |
| `docker/compose-gateway.yml` | VM instance-1 | gateway + auth-service |
| `docker/compose-backend.yml` | VM instance-2 | provider-service + ai-service |

Gateway env: `BACKEND_VM_IP` injected at deploy time so it can route to VM2 services.
Also fixed stale default: `GEMINI_MODEL` → `gemini-2.5-flash`, added `GEMINI_FALLBACK_MODEL`.

### Step 4 — Deploy script ✅

`docker/deploy-vm.sh`:
1. Reads VM IPs from `pulumi stack output`
2. Builds JARs locally with Maven
3. SCPs Dockerfiles, JARs, `.env`, compose files to each VM
4. Appends `BACKEND_VM_IP=$VM2_IP` to VM1's `.env`
5. Starts VM2 (backend) first, then VM1 (gateway)
6. Health-checks gateway at `http://<ip1>:8080/actuator/health`

---

## Phase 2 — Provision and Deploy

### Step 5 — Provision VMs ✅ DONE

```bash
cd healthcare-infra
make oracle-up
# exports: instance1PublicIp, instance2PublicIp
```

VMs are up. Get IPs with:
```bash
cd healthcare-infra/pulumi-oracle && pulumi stack output
```

### Step 6 — Deploy services ✅ DONE (2026-05-29)

```bash
./docker/deploy-vm.sh
```

Issues encountered and fixed during first deploy:
- Docker image builds on VM spiked CPU to 100% → fixed by sequential builds (one image at a time)
- JAR uploads dropped connection (VM OOM during SCP) → switched to `rsync -az --partial`
- Swap not persisting across reboots → added `/etc/fstab` entry in `setup-vm.sh`
- SSH timeouts during long uploads → added `ServerAliveInterval=15` to SSH_OPTS
- VM console password hardcoded in `compute.go` → moved to Pulumi config secret

Memory optimization applied to all compose files:
- `JAVA_TOOL_OPTIONS`: `-Xmx200m -Xms64m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC -XX:TieredStopAtLevel=1`
- Hikari pool: 5 → 2, Tomcat threads: 200 → 10, actuator: health endpoint only
- Result: ~270MB total for all 4 services (fits comfortably in 500MB RAM + 4GB swap per VM)

### Step 7 — Smoke test ✅ DONE

```bash
./integration_tests/run-it.sh auth   # 7/7 passing
./integration_tests/run-it.sh all    # full suite passing
```

---

## Phase 3 — eBPF EDR on instance-2

### Step 8 — GCP credentials for Oracle VM

Oracle VMs have no GCP compute SA — need explicit credentials for Cloud Logging + Pub/Sub.

```bash
# On local machine — in ebpfagent GCP project
gcloud iam service-accounts create healthcare-oracle-agent \
  --project=ebpfagent \
  --display-name="Healthcare Oracle VM eBPF agent"

gcloud iam service-accounts keys create /tmp/healthcare-oracle-agent.json \
  --iam-account=healthcare-oracle-agent@ebpfagent.iam.gserviceaccount.com

# Copy key to instance-2
scp -i ~/.ssh/oracle_vm /tmp/healthcare-oracle-agent.json opc@<ip2>:/etc/ebpf-creds.json
```

### Step 9 — Update eBPF infra/base.go

Uncomment and fill in the healthcare VM entry in `ebpf-edr-demo/infra/base.go`:

```go
// Healthcare Oracle VM — ebpf agent SA
_, err = projects.NewIAMMember(ctx, "healthcare-oracle-vm-logging-writer", &projects.IAMMemberArgs{
    Project: pulumi.String(project),
    Role:    pulumi.String("roles/logging.logWriter"),
    Member:  pulumi.String("serviceAccount:healthcare-oracle-agent@ebpfagent.iam.gserviceaccount.com"),
})

_, err = pubsub.NewTopicIAMMember(ctx, "healthcare-oracle-vm-pubsub-publisher", &pubsub.TopicIAMMemberArgs{
    Topic:   topic.Name,
    Project: pulumi.String(project),
    Role:    pulumi.String("roles/pubsub.publisher"),
    Member:  pulumi.String("serviceAccount:healthcare-oracle-agent@ebpfagent.iam.gserviceaccount.com"),
})
```

Then apply:
```bash
cd ebpf-edr-demo/infra
pulumi up
```

### Step 10 — Build and copy eBPF agent

OCI Free Tier = AMD64. Build for Linux AMD64:

```bash
cd ebpf-edr-demo
GOOS=linux GOARCH=amd64 go build -o ebpf-edr-linux-amd64 ./cmd/agent
scp -i ~/.ssh/oracle_vm ebpf-edr-linux-amd64 opc@<ip2>:~/ebpf-edr-demo
```

### Step 11 — Run agent on instance-2

```bash
ssh -i ~/.ssh/oracle_vm opc@<ip2>
sudo GOOGLE_APPLICATION_CREDENTIALS=/etc/ebpf-creds.json \
     GOOGLE_CLOUD_PROJECT=ebpfagent \
     ./ebpf-edr-demo
```

### Step 12 — Validate

Run existing `validate.sh` against instance-2, adapted for the Oracle VM environment.
Confirm alerts appear in Cloud Logging (`ebpfagent` project) and the Alert Router UI.

---

## Notes

- **HTTPS / TLS** — HTTP only on 8080 for now; add Nginx/Caddy reverse proxy later if needed.
- **Agent as systemd service** — run eBPF agent as a daemon (auto-restart on reboot) vs. manual for now.
