TERRAFORM_DIR := terraform
PULUMI_DIR    := pulumi-oracle

.PHONY: help \
        gcp-plan gcp-apply gcp-destroy \
        oracle-up oracle-destroy oracle-preview \
        pg-dump

help:
	@echo ""
	@echo "GCP (Terraform):"
	@echo "  make gcp-plan      — preview changes"
	@echo "  make gcp-apply     — apply changes"
	@echo "  make gcp-destroy   — destroy all GCP infra (prompts for confirmation)"
	@echo ""
	@echo "Oracle (Pulumi):"
	@echo "  make oracle-preview — preview changes"
	@echo "  make oracle-up      — provision Oracle VMs"
	@echo "  make oracle-destroy — destroy Oracle VMs (prompts for confirmation)"
	@echo ""
	@echo "Data:"
	@echo "  make pg-dump        — dump Cloud SQL database before destroying GCP"
	@echo ""

# ── GCP Terraform ─────────────────────────────────────────────────────────────

gcp-plan:
	cd $(TERRAFORM_DIR) && ./run-terraform.sh plan

gcp-apply:
	cd $(TERRAFORM_DIR) && ./run-terraform.sh apply

gcp-destroy:
	cd scripts && ./deploy-all.sh destroy-infra

# ── Oracle Pulumi ──────────────────────────────────────────────────────────────

oracle-preview:
	cd $(PULUMI_DIR) && pulumi preview

oracle-up:
	cd $(PULUMI_DIR) && pulumi up

oracle-destroy:
	cd $(PULUMI_DIR) && pulumi destroy

# ── Data ───────────────────────────────────────────────────────────────────────

# Dumps Cloud SQL via Cloud SQL Auth Proxy before GCP teardown.
# Requires: cloud-sql-proxy installed, GOOGLE_APPLICATION_CREDENTIALS set.
# Output: ./backups/healthcare-<timestamp>.sql
pg-dump:
	@echo "Backing up Cloud SQL before destroy..."
	@mkdir -p backups
	@DB_IP=$$(cd $(TERRAFORM_DIR) && terraform output -raw cloud_sql_private_ip 2>/dev/null); \
	if [ -z "$$DB_IP" ]; then echo "Skipping pg-dump — no Cloud SQL found"; exit 0; fi; \
	TIMESTAMP=$$(date +%Y%m%d-%H%M%S); \
	PGPASSWORD=$$(gcloud secrets versions access latest --secret=db-password 2>/dev/null) \
	pg_dump -h $$DB_IP -U postgres healthcare > backups/healthcare-$$TIMESTAMP.sql && \
	echo "Backup saved: backups/healthcare-$$TIMESTAMP.sql" || \
	echo "pg-dump failed — back up manually before destroying"
