# Legacy — Oracle Cloud VM deployment

Oracle Free Tier account was terminated 2026-06-06 due to VM loop creation policy violation.
All Oracle VMs (VM1: gateway + auth, VM2: provider + ai) were deleted.
The project is now deployed to GCP GKE (`health-ai-cluster-us-west1`).

Kept here for reference if Oracle Cloud is used again in the future.

## Contents

| Path | Description |
|------|-------------|
| `healthcare-infra/pulumi-oracle/` | Pulumi (Go) stack — OCI VCN, subnet, security list, 2× Micro VMs |
| `docker/setup-vm.sh` | Install Docker + eBPF EDR agent on Oracle VMs (run once after `pulumi up`) |
| `docker/deploy-vm.sh` | Build services locally and deploy to Oracle VMs via SSH |
| `docs/oracle-vm-runbook.md` | Day-to-day operations: start/stop services, update binary, check logs |
| `docs/deploy-oracle-plan.md` | Original deployment plan — completed before account termination |

## To re-enable Oracle Cloud

1. Create a new Oracle Cloud Free Tier account
2. Restore `healthcare-infra/pulumi-oracle/` to the active infra directory
3. Run `pulumi up` to provision VMs
4. Run `./docker/setup-vm.sh` to install Docker + eBPF agent
5. Run `./docker/deploy-vm.sh` to deploy services
