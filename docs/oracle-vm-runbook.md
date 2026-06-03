# Oracle VM Runbook

Everything is code-managed via Pulumi. No manual OCI Console changes — if a setting is needed,
add it to the Pulumi code and reprovision.

---

## Architecture

| VM | Pulumi name | Services | Public ports |
|----|-------------|----------|--------------|
| instance-1 (163.192.46.25) | `healthcare-instance-1` | gateway (8080) + auth-service (8082) | 8080, 22 |
| instance-2 (163.192.30.193) | `healthcare-instance-2` | provider-service (8083) + ai-service (8085) | 22 |

- Region: `us-sanjose-1`
- Shape: `VM.Standard.E2.1.Micro` (1 OCPU, ~500MB RAM) — OCI Always Free
- OS: Oracle Linux 9 (auto-discovered via `GetImages()`)
- SSH user: `opc`
- SSH key: `~/.ssh/oracle_vm` (private) / `~/.ssh/oracle_vm.pub` (public)
- SSH port: **22**
- Swap: 4GB swapfile at `/swapfile` (persisted in `/etc/fstab`)

Container runtime: **Podman** (Oracle Linux 9 native, Docker-compatible) + Docker Compose v2 binary.
`docker` CLI is aliased to `podman` via `podman-docker` — all `docker compose` commands work unchanged.

All network rules are defined in `healthcare-infra/pulumi-oracle/network.go`.
VM setup (Podman, swap, firewall) is handled by `docker/setup-vm.sh` — not Pulumi.

---

## Memory Budget

VMs have ~500MB RAM + 4GB swap each. Services are tuned to fit:

| VM | Services | RAM (idle) |
|----|----------|------------|
| VM1 | gateway + auth-service | ~110MB |
| VM2 | provider-service + ai-service | ~160MB |

JVM tuning applied via `JAVA_TOOL_OPTIONS` in compose files:
`-Xmx200m -Xms64m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC -XX:TieredStopAtLevel=1`

---

## One-time Setup

### 1. Generate SSH key pair (once per machine)
```bash
ssh-keygen -t rsa -b 2048 -f ~/.ssh/oracle_vm
```

### 2. Get OCI credentials
From OCI Console:
- `OCI_USER_OCID` → Profile (top-right) → User Settings → OCID
- `OCI_FINGERPRINT` → User Settings → API Keys → fingerprint column
- `OCI_TENANCY_OCID` → Governance → Tenancy details → OCID
- `OCI_REGION` → your home region (e.g. `us-sanjose-1`)
- `OCI_KEY_FILE` → full path to the PEM private key from API key creation
- `AVAILABILITY_DOMAIN` → Compute → Instances → Create instance → Placement

### 3. Fill in .env
```bash
cd healthcare-infra/pulumi-oracle
cp env.example .env
# edit .env with your values including VM_CONSOLE_PASSWORD
```

### 4. Run setup (writes ~/.oci/config + sets Pulumi stack config)
```bash
cd healthcare-infra/pulumi-oracle
./setup.sh
```

---

## Provision VMs

```bash
cd healthcare-infra
make oracle-up
```

Pulumi creates: VCN, Internet Gateway, Route Table, Security List, Subnet, 2× Instance.

After provisioning, run one-time VM setup (installs Podman, Docker Compose, configures swap):
```bash
./docker/setup-vm.sh
```

Get IPs anytime:
```bash
cd healthcare-infra/pulumi-oracle && pulumi stack output
```

---

## Deploy Services

### Prerequisites
- `docker/.env` exists (cp `docker/env.example` `docker/.env`, fill in values)
- JARs are buildable via Maven locally

```bash
./docker/deploy-vm.sh
```

The script:
1. Reads VM IPs from Pulumi stack output
2. Builds JARs locally with Maven
3. Clears VM buffer cache (frees RAM for uploads)
4. Uploads JARs via `rsync -az --partial` (compressed, resumable)
5. Copies Dockerfiles, compose files, and `.env` to each VM
6. Builds Docker images sequentially on each VM (one at a time to avoid CPU spikes)
7. Starts VM2 (backend) first, then VM1 (gateway)
8. Health-checks `http://<ip1>:8080/actuator/health`

### Compose-only redeploy (no code changes)
When only compose files changed (env vars, tuning), skip the full deploy:
```bash
scp -i ~/.ssh/oracle_vm docker/compose-gateway.yml opc@<ip1>:~/healthcare/docker/
scp -i ~/.ssh/oracle_vm docker/compose-backend.yml opc@<ip2>:~/healthcare/docker/
# Then restart on each VM (see Restart Services below)
```

---

## Smoke Test

```bash
./integration_tests/run-it.sh auth
./integration_tests/run-it.sh all
```

The gateway URL defaults to `http://163.192.46.25:8080` in `integration_tests/util/BaseIT.java`.

---

## Teardown

```bash
cd healthcare-infra
make oracle-destroy
```

Destroys all OCI resources. Safe to re-run `make oracle-up` to reprovision from scratch.

---

## View Logs

```bash
# Gateway + auth logs (VM1)
ssh -i ~/.ssh/oracle_vm opc@163.192.46.25 \
  "cd ~/healthcare/docker && "

# Backend logs (VM2)
ssh -i ~/.ssh/oracle_vm opc@163.192.30.193 \
  "cd ~/healthcare/docker && sudo docker compose -f compose-backend.yml logs --tail=50"
```

---

## Restart Services

```bash
# VM2 first (gateway depends on backend)
ssh -i ~/.ssh/oracle_vm opc@163.192.30.193 \
  "cd ~/healthcare/docker && sudo docker compose -f compose-backend.yml up -d"

# Then VM1
ssh -i ~/.ssh/oracle_vm opc@163.192.46.25 \
  "cd ~/healthcare/docker && sudo docker compose -f compose-gateway.yml up -d"
```

---

## Check Memory Usage

```bash
# VM1
ssh -i ~/.ssh/oracle_vm opc@163.192.46.25 "sudo docker stats --no-stream && free -h"

# VM2
ssh -i ~/.ssh/oracle_vm opc@163.192.30.193 "sudo docker stats --no-stream && free -h"
```

---

## Troubleshooting

### VM frozen — SSH banner hangs (TCP connects but no response)

**Symptoms**: `nc -zv <ip> 22` succeeds but `ssh` hangs at `Local version string SSH-2.0-OpenSSH_9.9`

**Cause**: VM OS is frozen, usually due to OOM during Docker image build (CPU/memory exhaustion)

**Fix**:
1. Check metrics in OCI Console → Instance → Metrics (look for CPU spike to 100%)
2. Go to OCI Console → Compute → Instances → Stop → Start (full power cycle)
3. Wait 2-3 minutes for sshd to become responsive
4. If still unresponsive: OCI Console → Instance → Console connection → Launch Cloud Shell connection (serial console bypass)

**Prevention**: Always use `deploy-vm.sh` which builds images sequentially. Never run `docker compose up --build` directly (builds all images in parallel → CPU spike → freeze).

### Services not running after VM reboot

Containers don't auto-restart yet (see BACKLOG). Manual restart:
```bash
# SSH into VM2 first, then VM1
sudo docker compose -f compose-backend.yml up -d   # VM2
sudo docker compose -f compose-gateway.yml up -d   # VM1
```

### JAR upload drops connection

`deploy-vm.sh` uses `rsync -az --partial` which resumes on failure. If it still fails:
```bash
# Clear buffer cache on VM first
ssh -i ~/.ssh/oracle_vm opc@<ip> "sudo sh -c 'sync; echo 3 > /proc/sys/vm/drop_caches'"
# Then retry deploy
./docker/deploy-vm.sh
```

### Port 8080 not reachable from outside

OCI security list only allows port 22 by default. To open port 8080:
- Add ingress rule in `healthcare-infra/pulumi-oracle/network.go`
- Run `make oracle-up` to apply
- Never change security rules manually in OCI Console
