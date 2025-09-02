package com.healthcare.constants;

/**
 * Database table names, column names, and index names
 */
public final class DatabaseConstants {

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_PATIENTS = "patients";
    public static final String TABLE_PROVIDERS = "providers";
    public static final String TABLE_APPOINTMENTS = "appointments";
    public static final String TABLE_MEDICAL_RECORDS = "medical_records";
    public static final String TABLE_AUDIT_LOGS = "audit_logs";

    // Column Names
    public static final String COL_ID = "id";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_PATIENT_ID = "patient_id";
    public static final String COL_PROVIDER_ID = "provider_id";
    public static final String COL_APPOINTMENT_ID = "appointment_id";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";

    // Index Names
    public static final String INDEX_USERS_EMAIL = "idx_users_email";
    public static final String INDEX_PATIENTS_USER_ID = "idx_patients_user_id";
    public static final String INDEX_PROVIDERS_USER_ID = "idx_providers_user_id";
    public static final String INDEX_APPOINTMENTS_PATIENT_ID = "idx_appointments_patient_id";
    public static final String INDEX_APPOINTMENTS_PROVIDER_ID = "idx_appointments_provider_id";
    public static final String INDEX_MEDICAL_RECORDS_PATIENT_ID = "idx_medical_records_patient_id";
    public static final String INDEX_MEDICAL_RECORDS_PROVIDER_ID = "idx_medical_records_provider_id";
    public static final String INDEX_AUDIT_LOGS_USER_ID = "idx_audit_logs_user_id";
    public static final String INDEX_AUDIT_LOGS_ACTION = "idx_audit_logs_action";

    private DatabaseConstants() {
        // Utility class
    }
}
