# Deployment Strategy — health-ai + ebpf-edr

Written: 2026-06-07  
Status: **Design — no implementation started**

---

## Goal

Run health-ai services and eBPF EDR monitoring in multiple environments with minimal
overhead. As a personal project, keep the infrastructure simple — scripts and YAML files,
no complex tooling beyond what already exists.

---

## Environments

| Env | Purpose | Cost | Status |
|-----|---------|------|--------|
| **Local Docker** | Day-to-day dev, integration tests | $0 | ✅ Working |
| **GCP GKE** | On-demand testing (bring up, test, destroy) | ~$1-2/mo | ✅ Working |
| **DigitalOcean Droplet** | Lightweight on-demand deploy, public access | ~$1-2/mo | 🔲 Planned |
| **Home Linux VM** | Future permanent free option | $0 | 🔲 Future |

---

## What is OUT OF SCOPE (for now)

- **eBPF on Kubernetes** — not needed right now. eBPF runs on the GCP Docker VM only,
  tested via `validate.sh`. The GKE DaemonSet exists but is not a priority.
- **eBPF on DigitalOcean** — not planned until the basic health-ai deploy works there first.
- **Pub/Sub, Cloud Logging infra** — existing setup on GCP VM stays as-is; no new GCP
  infra will be added for new environments.

---

## Health-AI Service Layout

4 Spring Boot services, all stateless, share one external Supabase PostgreSQL database:

```
gateway (8080)  ←── public entry point
   ├── auth-service (8082)
   ├── provider-service (8083)
   └── ai-service (8085)

Database: Supabase (external, always on, same instance for all envs)
```

Credentials (never in any repo):
- `docker/.env` — DB URL/user/pass, JWT keys, Gemini API key
- Same `.env` file is used for all environments — copy to each target host

---

## Phase 1 — Fix eBPF file sensor (current blocker)

**Scope:** ebpf-edr-demo only. Go-only change, no BPF C changes, no new deployment.

**Problem:** Ring buffer reader goroutine in `cmd/edr-monitor/main.go` exits silently on
any error. On GKE, Java startup activity causes a transient error that kills the goroutine —
file events (V3/V7/V9) never appear after that.

**Fix:** Add restart loop so the goroutine recovers instead of dying.

**Verification:** Deploy to GKE, run `./validate-gke.sh`, expect 9/9 passing.

---

## Phase 2 — DigitalOcean Droplet deploy for health-ai

**Scope:** health-ai only. Simple Docker Compose on a single Droplet.

### Approach

Single 2GB Droplet ($12/mo, billed per-second). Deploy health-ai with the existing
`docker/docker-compose.yml`. Destroy when not in use (~$1-2/mo actual cost).

### What needs to be built

**`scripts/do-deploy.sh`** — bring up a Droplet and deploy health-ai:
1. Create Droplet via `doctl` (DigitalOcean CLI), Docker 1-Click image
2. Wait for SSH to be ready
3. `scp docker/.env` and `docker-compose.yml` to Droplet
4. `ssh` → `docker compose up -d`
5. Print public IP + gateway URL

**`scripts/do-destroy.sh`** — tear down:
1. `doctl compute droplet delete <id>` — destroys Droplet, billing stops

**`scripts/do-status.sh`** — quick check:
1. List Droplets, show IP + running containers

### What does NOT change
- `docker-compose.yml` — unchanged, works as-is
- `docker/.env` — same file, same credentials, `scp`'d to Droplet
- Services — no changes needed

### Credentials on Droplet
- `docker/.env` is `scp`'d at deploy time, never committed to git
- Droplet is ephemeral — deleted after use, so no long-lived credential exposure
- `doctl` auth token stored locally in `~/.config/doctl/` (never in repo)

---

## Phase 3 — Keep GKE working (no changes needed)

The existing GKE deploy path stays exactly as-is:

```bash
cd kubernetes && ./deploy.sh all      # bring up GKE + health-ai
./validate-gke.sh                     # run eBPF tests
./deploy.sh destroy                   # tear down
```

No changes to `kubernetes/deploy.sh`, `kubernetes/pulumi/`, or GKE manifests.
GKE is the environment used for eBPF EDR testing. DigitalOcean is for lightweight
health-ai testing and public access demos.

---

## Phase 4 — Simplify GCP infra (future, after Phase 2 works)

Once DigitalOcean is working, revisit the GCP Pulumi `infra/` stack:

- The `infra/` stack provisions Cloud Logging, Pub/Sub, and IAM for the GCP Docker VM
- The GCP Docker VM already has `logging.logWriter` on its compute SA — this just works
- If Cloud Logging is not needed for new environments, `infra/` can move to `legacy/`
- **Do not touch until Phase 2 is confirmed working**

---

## File structure (target state after Phase 2)

```
health-ai/
├── docker/
│   ├── docker-compose.yml     ← local dev + single-VM deploy (unchanged)
│   └── env.example            ← template (unchanged)
├── kubernetes/
│   ├── deploy.sh              ← GKE deploy/destroy (unchanged)
│   └── pulumi/                ← GKE cluster infra (unchanged)
├── scripts/                   ← NEW: DigitalOcean deploy scripts
│   ├── do-deploy.sh
│   ├── do-destroy.sh
│   └── do-status.sh
└── docs/
    └── deploy-strategy.md     ← this file
```

---

## Decision log

| Decision | Reason |
|----------|--------|
| No eBPF on k8s for now | Not needed; GCP Docker VM handles all eBPF testing |
| Single Droplet (not DOKS) | Simpler, cheaper, docker-compose works without k8s |
| Same `docker-compose.yml` for all envs | No duplication; only the host changes |
| Same `.env` for all envs | Same Supabase DB, same credentials |
| Keep GKE untouched | Works today; only used for eBPF testing; don't break it |
| Retire GCP infra only after DO works | New solution must be verified before removing old |
