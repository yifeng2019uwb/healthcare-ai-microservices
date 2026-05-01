# AI Governance Playbook

How to apply the NIST AI RMF to this healthcare AI platform.
Follow these steps whenever a new AI model is added to the system.

---

## Step 1 — GOVERN: Register the model and assign ownership

Before any development starts:

1. Add the model to `ai-use-case-registry.md` with name, purpose, and intended users
2. Assign an owner (who is accountable for this model's behavior)
3. Confirm the use case is within acceptable risk tolerance
4. Get sign-off before proceeding to development

---

## Step 2 — MAP: Identify risks before building

Before writing any code:

1. Describe the data inputs — where they come from, who they represent
2. Document data lineage: source → load process → DB tables → training data
   - Example: Synthea generator → `healthcare-infra/synthea/` load scripts → patients/encounters/conditions tables → model training dataset
3. Identify impacted groups — which patients could be harmed by a wrong prediction
4. List potential harms — bias, privacy exposure, clinical risk
5. Add identified risks to `risk-register.md` with severity and owner
6. Confirm HIPAA applicability (all patient data in this system is PHI)

---

## Step 3 — MEASURE: Evaluate the model before deployment

Before deploying to production:

1. Write a model card in `governance/docs/` following `model-card-readmission.md` as example
2. Run bias evaluation — compare performance across age groups, gender
3. Document training data source, size, and date range
4. Set minimum performance thresholds — model does not deploy if below threshold
5. Record all results in the model card

---

## Step 4 — MANAGE: Wire up audit logging and monitoring

Before going live:

1. Confirm governance dashboard is recording every prediction (patient ID, model, result, timestamp, requesting provider)
2. Set a review cadence — audit log reviewed monthly
3. Define drift threshold — when model performance drops, trigger review
4. Define rollback trigger — if accuracy drops below threshold, redeploy previous tagged version
5. Tag the model version before deployment (e.g. `readmission-risk-v1.0`) so rollback is possible
6. Add model to incident response plan in `incident-response.md`
7. Document retirement criteria — under what conditions is the model retrained or removed

---

## Ongoing

| Cadence | Action |
|---------|--------|
| Every prediction | Audit log entry written automatically |
| Monthly | Review audit log for anomalies |
| Quarterly | Re-run bias evaluation on new data |
| On incident | Follow `incident-response.md` |
| On model update | Repeat Steps 3–4 for new version |
