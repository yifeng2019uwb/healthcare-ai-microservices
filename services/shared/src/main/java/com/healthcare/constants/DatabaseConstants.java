package com.healthcare.constants;

/**
 * Database table names, column names, and index names.
 * Single source of truth — all entity mappings reference these constants.
 */
public final class DatabaseConstants {

    private DatabaseConstants() {}

    // ==================== TABLE NAMES ====================

    public static final String TABLE_USERS          = "users";
    public static final String TABLE_PATIENTS       = "patients";
    public static final String TABLE_PROVIDERS      = "provider_profiles";
    public static final String TABLE_ENCOUNTERS     = "encounter";
    public static final String TABLE_CONDITIONS     = "conditions";
    public static final String TABLE_ALLERGIES       = "allergies";
    public static final String TABLE_AUDIT_LOGS     = "audit_logs";
    public static final String TABLE_ORGANIZATIONS  = "organizations";

    // ==================== COMMON COLUMNS ====================

    public static final String COL_ID         = "id";
    public static final String COL_USER_ID    = "user_id";
    public static final String COL_PATIENT_ID = "patient_id";
    public static final String COL_PROVIDER_ID  = "provider_id";
    public static final String COL_APPOINTMENT_ID = "appointment_id";
    public static final String COL_CREATED_AT   = "created_at";
    public static final String COL_UPDATED_AT   = "updated_at";
    public static final String COL_UPDATED_BY   = "updated_by";

    // ==================== USER COLUMNS ====================

    public static final String COL_USERNAME      = "username";
    public static final String COL_PASSWORD_HASH = "password_hash";
    public static final String COL_EMAIL         = "email";
    public static final String COL_ROLE          = "role";
    public static final String COL_IS_ACTIVE     = "is_active";

    // ==================== SHARED COLUMNS (used across multiple tables) ====================

    public static final String COL_FIRST_NAME = "first_name";
    public static final String COL_LAST_NAME  = "last_name";
    public static final String COL_PHONE      = "phone";
    public static final String COL_GENDER     = "gender";

    // ==================== PATIENT COLUMNS (Synthea + application) ====================

    // Synthea fields
    public static final String COL_BIRTHDATE             = "birthdate";
    public static final String COL_DEATHDATE             = "deathdate";
    public static final String COL_SSN                   = "ssn";
    public static final String COL_DRIVERS               = "drivers";
    public static final String COL_PASSPORT              = "passport";
    public static final String COL_PREFIX                = "prefix";
    public static final String COL_MIDDLE_NAME           = "middle_name";
    public static final String COL_SUFFIX                = "suffix";
    public static final String COL_MAIDEN                = "maiden";
    public static final String COL_MARITAL               = "marital";
    public static final String COL_RACE                  = "race";
    public static final String COL_ETHNICITY             = "ethnicity";
    public static final String COL_BIRTHPLACE            = "birthplace";
    public static final String COL_ADDRESS               = "address";
    public static final String COL_COUNTY                = "county";
    public static final String COL_STATE                 = "state";
    public static final String COL_CITY                  = "city";
    public static final String COL_FIPS                  = "fips";
    public static final String COL_ZIP                   = "zip";
    public static final String COL_LAT                   = "lat";
    public static final String COL_LON                   = "lon";
    public static final String COL_HEALTHCARE_EXPENSES   = "healthcare_expenses";
    public static final String COL_HEALTHCARE_COVERAGE   = "healthcare_coverage";
    public static final String COL_INCOME                = "income";

    // Application fields
    public static final String COL_AUTH_ID               = "auth_id";
    public static final String COL_MRN                   = "mrn";
    public static final String COL_EMERGENCY_CONTACT     = "emergency_contact";
    public static final String COL_BLOOD_TYPE            = "blood_type";

    // ==================== PROVIDER PROFILE COLUMNS ====================

    public static final String COL_LICENSE_NUMBERS = "license_numbers";
    public static final String COL_NPI_NUMBER      = "npi_number";
    public static final String COL_SPECIALTY       = "specialty";
    public static final String COL_QUALIFICATIONS  = "qualifications";
    public static final String COL_BIO             = "bio";
    public static final String COL_OFFICE_PHONE    = "office_phone";
    public static final String COL_ORGANIZATION_ID = "organization_id";
    public static final String COL_NAME            = "name";
    public static final String COL_SPECIALITY      = "speciality";
    public static final String COL_LICENSE_NUMBER  = "license_number";
    public static final String COL_PROVIDER_CODE   = "provider_code";
    public static final String COL_ENCOUNTERS      = "encounters";
    public static final String COL_PROCEDURES      = "procedures";

    // ==================== ORGANIZATIONS PROFILE COLUMNS ====================
    public static final String COL_REVENUE                  = "revenue";
    public static final String COL_UTILIZATION              = "utilization";

    // ==================== ENCOUNTER COLUMNS ====================

    public static final String COL_SCHEDULED_AT       = "scheduled_at";
    public static final String COL_CHECKIN_TIME        = "checkin_time";
    public static final String COL_STATUS              = "status";
    public static final String COL_APPOINTMENT_STATUS  = "status";
    public static final String COL_APPOINTMENT_TYPE    = "appointment_type";
    public static final String COL_NOTES               = "notes";
    public static final String COL_PAYER_ID            = "payer_id";
    public static final String COL_START_TIME          = "start_time";
    public static final String COL_STOP_TIME           = "stop_time";
    public static final String COL_ENCOUNTER_CLASS     = "encounter_class";
    public static final String COL_CODE                = "code";
    public static final String COL_DESCRIPTION         = "description";
    public static final String COL_BASE_COST           = "base_cost";
    public static final String COL_TOTAL_COST          = "total_cost";
    public static final String COL_PAYER_COVERAGE      = "payer_coverage";
    public static final String COL_REASON_CODE         = "reason_code";
    public static final String COL_REASON_DESC         = "reason_desc";
    public static final String COL_ENCOUNTER_STATUS    = "status";
    public static final String COL_ENCOUNTER_TYPE      = "encounter_type";

    // ==================== CONDITIONS COLUMNS ====================

    public static final String COL_RECORD_TYPE        = "record_type";
    public static final String COL_CONTENT            = "content";
    public static final String COL_IS_PATIENT_VISIBLE = "is_patient_visible";
    public static final String COL_RELEASE_DATE       = "release_date";
    public static final String COL_ENCOUNTER_ID       = "encounter_id";
    public static final String COL_STOP_DATE          = "stop_date";
    public static final String COL_SYSTEM             = "system";

    // ==================== ALLERGYS COLUMNS ====================

    public static final String COL_ALLERGY_TYPE        = "allergy_type";
    public static final String COL_START_DATE          = "col_start_date";
    public static final String COL_CATEGORY            = "category";
    public static final String COL_REACTION1           = "reaction1";
    public static final String COL_SEVERITY1           = "serverity1";
    public static final String COL_REACTION2           = "reaction2";
    public static final String COL_SEVERITY2           = "serverity2";
    public static final String COL_DESCRIPTION1        = "description1";
    public static final String COL_DESCRIPTION2        = "description2";

    // ==================== AUDIT LOG COLUMNS ====================

    public static final String COL_ACTION_TYPE   = "action_type";
    public static final String COL_RESOURCE_TYPE = "resource_type";
    public static final String COL_RESOURCE_ID   = "resource_id";
    public static final String COL_OUTCOME       = "outcome";
    public static final String COL_DETAILS       = "details";
    public static final String COL_SOURCE_IP     = "source_ip";
    public static final String COL_USER_AGENT    = "user_agent";
    public static final String COL_USER_ROLE     = "user_role";
    public static final String COL_ACTION        = "action";

    // ==================== INDEX NAMES ====================

    // users
    public static final String INDEX_USERS_EMAIL    = "idx_users_email";
    public static final String INDEX_USERS_USERNAME = "idx_users_username";
    public static final String INDEX_USERS_ROLE     = "idx_users_role";

    // patients
    public static final String INDEX_PATIENTS_AUTH_ID       = "idx_patients_auth_id";
    public static final String INDEX_PATIENTS_MRN           = "idx_patients_mrn";
    public static final String INDEX_PATIENTS_NAME          = "idx_patients_name";

    // provider_profiles
    public static final String INDEX_PROVIDERS_ORGANIZATION = "idx_providers_organization";
    public static final String INDEX_PROVIDERS_AUTH_ID      = "idx_providers_auth_id";
    public static final String INDEX_PROVIDERS_SPECIALITY   = "idx_providers_speciality";
    public static final String INDEX_PROVIDERS_CODE         = "idx_providers_code";
    public static final String INDEX_PROVIDERS_ACTIVE       = "idx_providers_active";
    public static final String INDEX_PROVIDERS_SPECIALTY    = "idx_provider_profiles_specialty";

    // Organization
    public static final String INDEX_ORGANIZATIONS_NAME = "organizations";

    // encounter
    public static final String INDEX_APPOINTMENTS_PROVIDER_SCHEDULE = "idx_provider_schedule";
    public static final String INDEX_APPOINTMENTS_PATIENT_SCHEDULE  = "idx_patient_schedule";
    public static final String INDEX_ENCOUNTERS_PATIENT             = "encounter_patient";
    public static final String INDEX_ENCOUNTERS_PROVIDER            = "encounter_provider";
    public static final String INDEX_ENCOUNTERS_START_TIME          = "encounters_start_time";

    // conditions
    public static final String INDEX_CONDITIONS_PATIENT  = "idx_conditions_patient";
    public static final String INDEX_CONDITIONS_CODE   = "idx_conditions_code";

    // conditions
    public static final String INDEX_ALLERGIES_PATIENT  = "idx_allergies_patient";
    public static final String INDEX_ALLERGIES_CATEGORY   = "idx_allergies_category";


    // audit_logs
    public static final String INDEX_AUDIT_AUTH_ID       = "idx_audit_auth_id";
    public static final String INDEX_AUDIT_RESOURCE   = "idx_audit_resource";
    public static final String INDEX_AUDIT_CREATED_AT = "idx_audit_created_at";

    // ==================== FOREIGN KEY NAMES ====================

    public static final String FK_PATIENT_USER        = "patient_profiles_user_id_fkey";
    public static final String FK_PROVIDER_USER       = "provider_profiles_user_id_fkey";
    public static final String FK_APPOINTMENT_PATIENT = "appointments_patient_id_fkey";
    public static final String FK_APPOINTMENT_PROVIDER = "appointments_provider_id_fkey";
    public static final String FK_MEDICAL_RECORD_APPOINTMENT = "medical_records_appointment_id_fkey";

    // ==================== COLUMN DEFINITIONS ====================

    public static final String COLUMN_DEFINITION_JSONB       = "JSONB";
    public static final String COLUMN_DEFINITION_TIMESTAMPTZ = "TIMESTAMPTZ";
    public static final String COLUMN_DEFINITION_TEXT        = "TEXT";
    public static final String COLUMN_DEFINITION_INET        = "INET";

    public static final String COLUMN_DEFINITION_GENDER_ENUM             = "gender_enum";
    public static final String COLUMN_DEFINITION_STATUS_ENUM             = "status_enum";
    public static final String COLUMN_DEFINITION_ROLE_ENUM               = "role_enum";
    public static final String COLUMN_DEFINITION_APPOINTMENT_STATUS_ENUM = "appointment_status_enum";

    public static final String COLUMN_DEFINITION_APPOINTMENT_STATUS_WITH_CHECK =
            "VARCHAR(20) CHECK (status IN ('AVAILABLE','SCHEDULED','CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED','NO_SHOW'))";

    // ==================== ENTITY GRAPH NAMES ====================

    public static final String ENTITY_GRAPH_APPOINTMENT_WITH_PATIENT_AND_PROVIDER = "Appointment.withPatientAndProvider";
    public static final String ENTITY_GRAPH_APPOINTMENT_WITH_MEDICAL_RECORDS      = "Appointment.withMedicalRecords";
    public static final String ENTITY_GRAPH_APPOINTMENT_FULL_DETAILS              = "Appointment.fullDetails";

    public static final String ENTITY_GRAPH_PATIENT_WITH_USER        = "Patient.withUser";
    public static final String ENTITY_GRAPH_PATIENT_WITH_APPOINTMENTS = "Patient.withAppointments";
    public static final String ENTITY_GRAPH_PATIENT_FULL_DETAILS     = "Patient.fullDetails";

    public static final String ENTITY_GRAPH_PROVIDER_WITH_USER        = "Provider.withUser";
    public static final String ENTITY_GRAPH_PROVIDER_WITH_APPOINTMENTS = "Provider.withAppointments";
    public static final String ENTITY_GRAPH_PROVIDER_FULL_DETAILS     = "Provider.fullDetails";

    public static final String ENTITY_GRAPH_MEDICAL_RECORD_WITH_APPOINTMENT = "MedicalRecord.withAppointment";
    public static final String ENTITY_GRAPH_MEDICAL_RECORD_FULL_DETAILS     = "MedicalRecord.fullDetails";

    // ==================== ENTITY ATTRIBUTE NAMES ====================

    public static final String ATTR_PATIENT        = "patient";
    public static final String ATTR_PROVIDER       = "provider";
    public static final String ATTR_APPOINTMENT    = "appointment";
    public static final String ATTR_MEDICAL_RECORDS = "medicalRecords";
    public static final String ATTR_APPOINTMENTS   = "appointments";
    public static final String ATTR_USER           = "user";
    public static final String ATTR_AUDIT_LOGS     = "auditLogs";

    // ==================== COLUMN LENGTHS (LEN_) ====================
    // Matches exactly the VARCHAR sizes in SQL schema files

    // users
    public static final int LEN_USERNAME      = 100;
    public static final int LEN_EMAIL         = 255;
    public static final int LEN_PASSWORD_HASH = 255;
    public static final int LEN_ROLE          = 20;
    public static final int LEN_UPDATED_BY    = 100;

    // patients — Synthea fields
    public static final int LEN_SSN           = 20;
    public static final int LEN_DRIVERS       = 20;
    public static final int LEN_PASSPORT      = 20;
    public static final int LEN_PREFIX        = 10;
    public static final int LEN_FIRST_NAME    = 100;
    public static final int LEN_MIDDLE_NAME   = 100;
    public static final int LEN_LAST_NAME     = 100;
    public static final int LEN_SUFFIX        = 10;
    public static final int LEN_MAIDEN        = 100;
    public static final int LEN_MARITAL       = 1;
    public static final int LEN_RACE          = 50;
    public static final int LEN_ETHNICITY     = 50;
    public static final int LEN_GENDER        = 10;
    public static final int LEN_BIRTHPLACE    = 255;
    public static final int LEN_ADDRESS       = 255;
    public static final int LEN_CITY          = 100;
    public static final int LEN_STATE         = 50;
    public static final int LEN_COUNTY        = 100;
    public static final int LEN_FIPS          = 20;
    public static final int LEN_ZIP           = 10;

    // patients — application fields
    public static final int LEN_MRN               = 20;
    public static final int LEN_PHONE             = 20;
    public static final int LEN_EMERGENCY_CONTACT = 255;
    public static final int LEN_BLOOD_TYPE        = 5;

    // providers
    public static final int LEN_PROVIDER_NAME     = 255;
    public static final int LEN_PROVIDER_GENDER   = 1;
    public static final int LEN_SPECIALITY        = 100;
    public static final int LEN_PROVIDER_CODE     = 20;
    public static final int LEN_LICENSE_NUMBER    = 50;

    // organizations
    public static final int LEN_ORGANIZATION_NAME = 255;
    public static final int LEN_ORG_ADDRESS       = 255;
    public static final int LEN_ORG_CITY          = 100;
    public static final int LEN_ORG_STATE         = 50;
    public static final int LEN_ORG_ZIP           = 20;
    public static final int LEN_ORG_PHONE         = 50;

    // encounters
    public static final int LEN_PAYER_ID        = 36;
    public static final int LEN_ENCOUNTER_CLASS = 50;
    public static final int LEN_CODE            = 20;
    public static final int LEN_DESCRIPTION     = 255;
    public static final int LEN_REASON_CODE     = 20;
    public static final int LEN_REASON_DESC     = 255;
    public static final int LEN_ENCOUNTER_STATUS = 10;
    public static final int LEN_ENCOUNTER_TYPE  = 10;

    // conditions
    public static final int LEN_SYSTEM          = 20;

    // allergies
    public static final int LEN_ALLERGY_TYPE    = 20;
    public static final int LEN_CATEGORY        = 20;
    public static final int LEN_REACTION        = 20;
    public static final int LEN_SEVERITY        = 10;

    // audit_logs
    public static final int LEN_AUDIT_AUTH_ID     = 128;
    public static final int LEN_USER_ROLE         = 20;
    public static final int LEN_ACTION            = 10;
    public static final int LEN_RESOURCE_TYPE     = 50;
    public static final int LEN_OUTCOME           = 10;

    public static final int LEN_NOTES            = 2000;

    // ==================== DECIMAL PRECISION AND SCALE (LEN_) ====================

    public static final int LEN_LAT_LON_PRECISION    = 10;
    public static final int LEN_LAT_LON_SCALE        = 6;
    public static final int LEN_HEALTHCARE_PRECISION = 12;
    public static final int LEN_COST_PRECISION       = 10;
    public static final int LEN_MONEY_SCALE          = 2;

}