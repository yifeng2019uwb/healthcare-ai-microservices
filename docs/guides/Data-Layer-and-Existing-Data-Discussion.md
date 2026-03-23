# Data Layer First vs Refactor Later — and Using Existing Data

**No code.** This doc is a planning discussion: how to order the data layer work and how to let patients and providers **create and use** data, including **existing data from somewhere**.

---

## 1. Complete data layer first vs refactor later

### Option A: Complete the data layer first
- **Idea**: Finish repositories/DAOs, service layer, and any migrations for all core tables (user_profiles, patient_profiles, provider_profiles, appointments, medical_records, audit_logs) before building “create and use” flows.
- **Pros**: One clear foundation; fewer schema surprises later; aligns with BACKLOG (foundation → auth → patient/provider).
- **Cons**: Slower to see end-to-end value; “existing data from somewhere” might require extra tables or columns we haven’t planned yet, so we could still do a small refactor when we add that.

### Option B: Refactor later
- **Idea**: Get a minimal path working (e.g. only User + Patient + Provider create/read), then add the rest of the data layer and fix schema as needed.
- **Pros**: Fast feedback; you see patient and provider creating/using data soon.
- **Cons**: Schema or API changes later can be costly (migrations, many services). In healthcare, changing identity/profile models is especially painful.

### Recommended: **Business usage first, then minimal data layer, then iterate**
- **Do not** lock the whole data layer in isolation. **Do** define **who** does **what** with **what data** (and where “existing data” comes from), then implement the **smallest data layer** that supports that.
- **Then** add more (appointments, medical records, audit, etc.) in steps. Refactor only when a new use case forces a schema change.
- So: not “data layer 100% then features,” and not “hack first and refactor everything later.” Middle path: **clear usage → minimal data layer for that usage → expand**.

---

## 2. Clear business usage first

Before touching the data layer we should write down:

### 2.1 Who creates what
- **Patient**: Creates/updates own **user_profile** + **patient_profile** (e.g. registration, profile edit). Maybe “creates” a booking (appointment) from existing slots.
- **Provider**: Creates/updates own **user_profile** + **provider_profile**. Creates **appointment slots**, **medical_records** (after a visit). Maybe creates/updates reference data (e.g. specialties, locations) if we decide that’s provider-managed.

### 2.2 Who uses (reads) what
- **Patient**: Reads own profile, own appointments, own medical records (when allowed).
- **Provider**: Reads own profile, list of (their) patients, appointments, medical records for patients they care for.

### 2.3 Where does “existing data” come from?
This drives whether we need more schema or just seed/import:

- **Reference/lookup data** (e.g. specialties, locations, insurance codes)  
  - Source: Curated list (we own), or external standard (e.g. FHIR, NPPES).  
  - Usage: Drop-downs, filters; patients and providers **use** it when creating profiles or appointments.  
  - Data layer impact: Either columns/FKs on existing tables, or small “reference” tables + minimal CRUD. No need for full generic “data layer” for everything at once.

- **Seed/demo data** (sample patients, providers, appointments)  
  - Source: Scripts or migrations (e.g. SQL, Flyway/Liquibase).  
  - Usage: So that patient and provider can **use** the app without creating everything from scratch.  
  - Data layer impact: Same tables we already have; no new entities. Need a **seed strategy** (when to run, envs).

- **Imported data** (e.g. “existing” patients from CSV, or from another system)  
  - Source: File upload or API from external system.  
  - Usage: **Provider** (or admin) imports; **patient** and **provider** then **use** that data in the app.  
  - Data layer impact: Same core tables; might need an “import_jobs” or “data_sources” table later for traceability. Format and validation rules matter more than new core entities.

- **External system of record** (e.g. EHR we read from)  
  - Source: API or feed from another system.  
  - Usage: We **use** their data (display, sync); maybe **create** local copies in our DB.  
  - Data layer impact: Could be sync tables or caches; design depends on consistency and latency requirements.

So: **define “existing data from somewhere”** as one (or more) of the above. That tells us whether we need reference tables, seed scripts, import APIs, or sync — and whether the **current** schema is enough or we need a small extension.

---

## 3. How this affects the data layer

- **Current state** (from BACKLOG and shared README):  
  - Entities and DAO interfaces exist for the 6 core tables.  
  - Repository layer (CRUD) may still be “next” in some places; service layer (BaseService, entity services) is not done.

- **If “existing data” = reference + seed only**:  
  - We can **complete a minimal data layer** for: **User**, **Patient**, **Provider** (and maybe **Appointment** if we need slots for “use”).  
  - Add a **seed strategy** (scripts or migrations) that inserts reference data + demo patients/providers.  
  - Patients and providers **create** (registration, profile) and **use** (view, book, etc.) that data. No need to “complete” medical_records or audit_logs data layer before allowing create/use for profiles and appointments.

- **If “existing data” = import from file/system**:  
  - Same minimal data layer; add **one** clear use case (e.g. “provider uploads CSV of patients” or “we sync providers from X”).  
  - Define format and validation; implement import as a single flow. Refine schema only if we discover we need extra fields or tables (e.g. import history).

So: **optimize by scope**. Don’t “complete the entire data layer” before any business usage. Do “complete the slice of data layer needed for the chosen usage + existing data source.”

---

## 4. Suggested steps (in order)

1. **Write down business usage (one-pager)**  
   - Who (patient, provider) creates what.  
   - Who uses what (read/update).  
   - List “existing data from somewhere”: reference data, seed/demo, import, or external system.  
   - No code; just sentences and maybe a small table.

2. **Decide “existing data” source(s)**  
   - Pick one primary source first (e.g. “seed data + reference lists”).  
   - Decide where it lives (SQL scripts, Flyway, admin API, etc.) and who can trigger or use it (e.g. only in dev vs production seed).

3. **Map to current schema**  
   - Check database-design and shared entities: do we already have tables/columns for that usage and that data?  
   - Note gaps (e.g. “we need a specialties table” or “we need an import_jobs table”).  
   - If gaps are small, add them in the next step; if large, reconsider scope.

4. **Define “minimal data layer” for that usage**  
   - Example: repositories/DAOs and one service layer for **User**, **Patient**, **Provider** (and **Appointment** if needed for “use”).  
   - Leave **medical_records** and **audit_logs** for a later step unless the first use case needs them.  
   - Document: “We will implement up to X; Y and Z come later.”

5. **Define seed/reference strategy**  
   - How do reference data and demo patients/providers get into the DB (migrations, script, API)?  
   - Who can create/update reference data (e.g. only migrations vs provider-editable).  
   - Ensure both patient and provider can **use** this data (e.g. dropdowns, listing providers).

6. **Implement in order**  
   - Implement the minimal data layer (repositories + services for the chosen entities).  
   - Add seed/reference (and optionally one import path).  
   - Then add APIs so patient and provider can create and use that data (registration, profile, maybe booking).  
   - After that, extend to more entities (e.g. medical records, full audit) when a new use case appears.

7. **Refactor only when needed**  
   - If a new use case or “existing data” source forces a schema change, do a small refactor (migration + service/API tweaks). Avoid big-bang “complete data layer” or “refactor everything later.”

---

## 5. Summary

| Question | Recommendation |
|----------|-----------------|
| Data layer first or refactor later? | **Neither.** Define business usage and “existing data” first; then implement **minimal data layer** for that; then iterate. |
| What comes first? | **Clear business usage** (who creates what, who uses what, where existing data comes from). |
| How do we allow patient and provider to create and use data? | By implementing the **minimal slice** of data layer + seed/reference (and optional import) that supports that usage, then exposing it via APIs with clear roles (patient vs provider). |
| Where does “existing data” live? | Decide: reference tables + seed scripts, or import flow, or external sync — then design only that part; no need to build everything up front. |

When you’re ready, the next concrete step is **Step 1**: write the one-pager (business usage + “existing data from somewhere”) and share it; then we can align the data layer and implementation tasks to it.
