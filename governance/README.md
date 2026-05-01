# AI Governance

NIST AI Risk Management Framework (AI RMF) applied to this healthcare AI platform.

Purpose: Practice for AIGP certification. Demonstrates ability to apply governance frameworks to a real technical system.

---

## Structure

```
governance/
  docs/        NIST AI RMF artifacts (documents, policies, risk register)
  dashboard/   Governance microservice (audit log API, model registry)
```

---

## docs/ — What goes here

| File | NIST Function | Description |
|------|--------------|-------------|
| `nist-ai-rmf-template.md` | All | Framework template — what each function requires, artifacts needed, questions to answer |
| `playbook.md` | All | How to apply the framework to this project — step-by-step for onboarding a new AI model |
| `ai-governance-policy.md` | GOVERN | Roles, accountability, decision authority |
| `ai-use-case-registry.md` | MAP | What AI does, who uses it, intended context |
| `risk-register.md` | MAP | Identified risks, severity, owner |
| `model-card-readmission.md` | MEASURE | Readmission risk model — inputs, outputs, bias evaluation |
| `incident-response.md` | MANAGE | What to do when AI produces wrong or harmful output |

---

## dashboard/ — What goes here

Python/FastAPI microservice. Visualizes the four NIST AI RMF core functions as a live dashboard:

- **GOVERN** — policies, roles, accountability status
- **MAP** — AI use cases, risk inventory, data lineage
- **MEASURE** — model performance metrics, bias indicators
- **MANAGE** — active incidents, audit log, decisions made, rollback status

Confirmed features:
- **Audit Log API** — every AI prediction recorded: patient ID, model name, result, timestamp, requesting provider
- **Model Registry** — registered models with version, owner, and deployment status

API design TBD — to be defined when implementation starts.

---

## NIST AI RMF — Quick Reference

- **GOVERN** — policies, roles, accountability structures
- **MAP** — identify context, risks, and potential harms before deployment
- **MEASURE** — evaluate model performance, fairness, and bias
- **MANAGE** — respond to risks, monitor in production, handle incidents

---

## Status

| Item | Status |
|------|--------|
| `docs/` artifacts | Not started |
| `dashboard/` service | Not started |
| AI model (readmission risk) | Not started — prerequisite for MEASURE docs |
| Synthea data load | Not started — prerequisite for AI model |
