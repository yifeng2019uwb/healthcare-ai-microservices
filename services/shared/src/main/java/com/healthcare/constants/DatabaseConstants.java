package com.healthcare.constants;

/**
 * Database table names, column names, and index names
 */
public final class DatabaseConstants {

    // Table Names
    public static final String TABLE_USER_PROFILES = "user_profiles";
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
    public static final String COL_UPDATED_BY = "updated_by";

    // User Profile Columns
    public static final String COL_EXTERNAL_AUTH_ID = "external_auth_id";
    public static final String COL_FIRST_NAME = "first_name";
    public static final String COL_LAST_NAME = "last_name";
    public static final String COL_EMAIL = "email";
    public static final String COL_PHONE = "phone";
    public static final String COL_DATE_OF_BIRTH = "date_of_birth";
    public static final String COL_GENDER = "gender";
    public static final String COL_STREET_ADDRESS = "street_address";
    public static final String COL_CITY = "city";
    public static final String COL_STATE = "state";
    public static final String COL_POSTAL_CODE = "postal_code";
    public static final String COL_COUNTRY = "country";
    public static final String COL_ROLE = "role";
    public static final String COL_USER_STATUS = "status";
    public static final String COL_CUSTOM_DATA = "custom_data";

    // Patient Profile Columns
    public static final String COL_PATIENT_NUMBER = "patient_number";
    public static final String COL_MEDICAL_HISTORY = "medical_history";
    public static final String COL_ALLERGIES = "allergies";
    public static final String COL_CURRENT_MEDICATIONS = "current_medications";
    public static final String COL_INSURANCE_PROVIDER = "insurance_provider";
    public static final String COL_INSURANCE_POLICY_NUMBER = "insurance_policy_number";
    public static final String COL_EMERGENCY_CONTACT_NAME = "emergency_contact_name";
    public static final String COL_EMERGENCY_CONTACT_PHONE = "emergency_contact_phone";
    public static final String COL_PRIMARY_CARE_PHYSICIAN = "primary_care_physician";

    // Provider Profile Columns
    public static final String COL_LICENSE_NUMBERS = "license_numbers";
    public static final String COL_NPI_NUMBER = "npi_number";
    public static final String COL_SPECIALTY = "specialty";
    public static final String COL_QUALIFICATIONS = "qualifications";
    public static final String COL_BIO = "bio";
    public static final String COL_OFFICE_PHONE = "office_phone";

    // Appointment Columns
    public static final String COL_SCHEDULED_AT = "scheduled_at";
    public static final String COL_CHECKIN_TIME = "checkin_time";
    public static final String COL_APPOINTMENT_STATUS = "status";
    public static final String COL_APPOINTMENT_TYPE = "appointment_type";
    public static final String COL_NOTES = "notes";

    // Medical Record Columns
    public static final String COL_RECORD_TYPE = "record_type";
    public static final String COL_CONTENT = "content";
    public static final String COL_IS_PATIENT_VISIBLE = "is_patient_visible";
    public static final String COL_RELEASE_DATE = "release_date";

    // Audit Log Columns
    public static final String COL_ACTION_TYPE = "action_type";
    public static final String COL_RESOURCE_TYPE = "resource_type";
    public static final String COL_RESOURCE_ID = "resource_id";
    public static final String COL_OUTCOME = "outcome";
    public static final String COL_DETAILS = "details";
    public static final String COL_SOURCE_IP = "source_ip";
    public static final String COL_USER_AGENT = "user_agent";

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
