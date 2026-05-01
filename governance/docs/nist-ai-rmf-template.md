# NIST AI RMF Template

Reference template for applying the NIST AI Risk Management Framework.
Fill in each section as the project progresses.

---

## GOVERN — Establish accountability and policies

**Purpose:** Set up the structures, policies, and roles that make AI risk management possible.

### Questions to answer
- Who is responsible for AI decisions in this system?
- What policies govern how AI is developed and deployed?
- What are the acceptable risk thresholds?
- How are AI-related decisions documented and reviewed?

### Artifacts needed
- [ ] Roles and accountability matrix
- [ ] AI acceptable use policy
- [ ] Risk tolerance statement
- [ ] Review and approval process

---

## MAP — Identify context and risks

**Purpose:** Understand what the AI system does, who it affects, and what could go wrong.

### Questions to answer
- What problem is the AI solving?
- Who are the users and who is impacted by the AI output?
- What data is used? Where does it come from? How was it transformed?
- What are the potential harms (bias, safety, privacy)?
- What regulations apply (HIPAA, etc.)?

### Artifacts needed
- [ ] AI use case description
- [ ] Stakeholder and impact map
- [ ] Data lineage — source → load process → DB tables → model training data
- [ ] Data source inventory
- [ ] Risk identification list

---

## MEASURE — Evaluate performance and bias

**Purpose:** Test and measure whether the AI is working as intended and identify problems.

### Questions to answer
- How accurate is the model?
- Are there performance differences across patient groups (age, gender, race)?
- How was the model tested and validated?
- What metrics indicate the model is degrading?

### Artifacts needed
- [ ] Model card (inputs, outputs, training data, metrics)
- [ ] Bias evaluation results
- [ ] Performance benchmarks
- [ ] Monitoring plan

---

## MANAGE — Respond to and monitor risks

**Purpose:** Put plans in place to respond when something goes wrong and monitor AI in production.

### Questions to answer
- What happens if the AI produces a wrong prediction?
- Who is notified and what is the escalation path?
- How is the audit log reviewed?
- When is a model retrained or retired?
- What triggers a rollback to a previous model version?

### Artifacts needed
- [ ] Incident response plan
- [ ] Audit log policy (every prediction recorded with patient ID, model, result, timestamp)
- [ ] Rollback strategy — version tagging, trigger thresholds, rollback process
- [ ] Model update and retirement criteria
- [ ] Ongoing monitoring checklist
