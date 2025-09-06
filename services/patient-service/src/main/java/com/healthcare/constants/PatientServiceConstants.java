package com.healthcare.constants;

/**
 * Constants for Patient Service
 *
 * Centralized constants to avoid hardcoded values throughout the service.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
public final class PatientServiceConstants {

    // Private constructor to prevent instantiation
    private PatientServiceConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // Service Information
    public static final String SERVICE_NAME = "patient-service";
    public static final String SERVICE_VERSION = "1.0.0";
    public static final String SERVICE_STATUS_UP = "UP";
    public static final String SERVICE_STATUS_DOWN = "DOWN";

    // API Endpoints
    public static final String HEALTH_ENDPOINT = "/health";
    public static final String CREATE_PATIENT_ENDPOINT = "/api/patients";
    public static final String GET_PROFILE_ENDPOINT = "/api/patients/profile";
    public static final String UPDATE_PROFILE_ENDPOINT = "/api/patients/profile";
    public static final String UPDATE_PATIENT_INFO_ENDPOINT = "/api/patients/patient-info";
    public static final String GET_MEDICAL_HISTORY_ENDPOINT = "/api/patients/medical-history";

    // API Response Messages
    public static final String ACCOUNT_CREATED_SUCCESS = "Account created successfully";
    public static final String PROFILE_UPDATED_SUCCESS = "Profile updated successfully";
    public static final String PATIENT_INFO_UPDATED_SUCCESS = "Patient information updated successfully";

    // Error Messages
    public static final String ERROR_BAD_REQUEST = "BAD_REQUEST";
    public static final String ERROR_CONFLICT = "CONFLICT";
    public static final String ERROR_UNAUTHORIZED = "UNAUTHORIZED";
    public static final String ERROR_FORBIDDEN = "FORBIDDEN";
    public static final String ERROR_NOT_FOUND = "NOT_FOUND";
    public static final String ERROR_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    // Error Descriptions
    public static final String ERROR_INVALID_REQUEST_DATA = "Invalid request data or missing required fields";
    public static final String ERROR_USER_ALREADY_EXISTS = "User or email already exists";
    public static final String ERROR_INVALID_JWT_TOKEN = "Invalid or expired JWT token";
    public static final String ERROR_ACCOUNT_SUSPENDED = "Account is suspended";
    public static final String ERROR_PATIENT_NOT_FOUND = "Patient profile not found or account is inactive";
    public static final String ERROR_INSUFFICIENT_PERMISSIONS = "Insufficient permissions to update medical information";
    public static final String ERROR_INVALID_PHONE_FORMAT = "Invalid request data or phone number format is invalid";
    public static final String ERROR_INVALID_MEDICAL_DATA = "Invalid medical data format";
    public static final String ERROR_PATIENT_ROLE_REQUIRED = "Insufficient permissions. Patient role required";
    public static final String ERROR_UNEXPECTED_ACCOUNT_CREATION = "An unexpected error occurred while creating patient account";
    public static final String ERROR_UNEXPECTED_PROFILE_RETRIEVAL = "An unexpected error occurred while retrieving profile";
    public static final String ERROR_UNEXPECTED_PROFILE_UPDATE = "An unexpected error occurred while updating profile";
    public static final String ERROR_UNEXPECTED_PATIENT_INFO_UPDATE = "An unexpected error occurred while updating patient information";
    public static final String ERROR_UNEXPECTED_GENERAL = "An unexpected error occurred";

    // Validation Patterns
    public static final String PATTERN_PHONE_E164 = "^\\+?[1-9]\\d{1,14}$";
    public static final String PATTERN_NAME_LETTERS_ONLY = "^[\\p{L}\\s'-]{2,100}$";
    public static final String PATTERN_GENDER_VALUES = "^(MALE|FEMALE|OTHER|UNKNOWN)$";

    // Field Lengths
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 255;
    public static final int MAX_PHONE_LENGTH = 20;
    public static final int MAX_ADDRESS_LENGTH = 255;
    public static final int MAX_CITY_LENGTH = 100;
    public static final int MAX_STATE_LENGTH = 50;
    public static final int MAX_POSTAL_CODE_LENGTH = 20;
    public static final int MAX_COUNTRY_LENGTH = 50;
    public static final int MAX_EMERGENCY_CONTACT_NAME_LENGTH = 100;
    public static final int MAX_INSURANCE_PROVIDER_LENGTH = 100;
    public static final int MAX_INSURANCE_POLICY_LENGTH = 50;
    public static final int MAX_PHYSICIAN_LENGTH = 100;

    // User Roles
    public static final String ROLE_PATIENT = "PATIENT";
    public static final String ROLE_PROVIDER = "PROVIDER";
    public static final String ROLE_ADMIN = "ADMIN";

    // User Status
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_SUSPENDED = "SUSPENDED";

    // Gender Values
    public static final String GENDER_MALE = "MALE";
    public static final String GENDER_FEMALE = "FEMALE";
    public static final String GENDER_OTHER = "OTHER";
    public static final String GENDER_UNKNOWN = "UNKNOWN";

    // Appointment Status
    public static final String APPOINTMENT_STATUS_COMPLETED = "COMPLETED";
    public static final String APPOINTMENT_STATUS_UPCOMING = "UPCOMING";
    public static final String APPOINTMENT_STATUS_SCHEDULED = "SCHEDULED";
    public static final String APPOINTMENT_STATUS_CANCELLED = "CANCELLED";

    // Medical Record Types
    public static final String RECORD_TYPE_DIAGNOSIS = "DIAGNOSIS";
    public static final String RECORD_TYPE_TREATMENT = "TREATMENT";
    public static final String RECORD_TYPE_PRESCRIPTION = "PRESCRIPTION";
    public static final String RECORD_TYPE_LAB_RESULT = "LAB_RESULT";

    // Allergy Severity
    public static final String ALLERGY_SEVERITY_SEVERE = "SEVERE";
    public static final String ALLERGY_SEVERITY_MODERATE = "MODERATE";
    public static final String ALLERGY_SEVERITY_MILD = "MILD";

    // Database Dependencies
    public static final String DEPENDENCY_DATABASE = "database";
    public static final String DEPENDENCY_AUTH_SERVICE = "auth-service";

    // JSON Field Names
    public static final String JSON_FIELD_EXTERNAL_USER_ID = "externalUserId";
    public static final String JSON_FIELD_FIRST_NAME = "firstName";
    public static final String JSON_FIELD_LAST_NAME = "lastName";
    public static final String JSON_FIELD_EMAIL = "email";
    public static final String JSON_FIELD_PHONE = "phone";
    public static final String JSON_FIELD_DATE_OF_BIRTH = "dateOfBirth";
    public static final String JSON_FIELD_GENDER = "gender";
    public static final String JSON_FIELD_STREET_ADDRESS = "streetAddress";
    public static final String JSON_FIELD_CITY = "city";
    public static final String JSON_FIELD_STATE = "state";
    public static final String JSON_FIELD_POSTAL_CODE = "postalCode";
    public static final String JSON_FIELD_COUNTRY = "country";
    public static final String JSON_FIELD_EMERGENCY_CONTACT_NAME = "emergencyContactName";
    public static final String JSON_FIELD_EMERGENCY_CONTACT_PHONE = "emergencyContactPhone";
    public static final String JSON_FIELD_PRIMARY_CARE_PHYSICIAN = "primaryCarePhysician";
    public static final String JSON_FIELD_INSURANCE_PROVIDER = "insuranceProvider";
    public static final String JSON_FIELD_INSURANCE_POLICY_NUMBER = "insurancePolicyNumber";
    public static final String JSON_FIELD_MEDICAL_HISTORY = "medicalHistory";
    public static final String JSON_FIELD_ALLERGIES = "allergies";
    public static final String JSON_FIELD_PATIENT_NUMBER = "patientNumber";

    // Medical History Field Names
    public static final String JSON_FIELD_CONDITIONS = "conditions";
    public static final String JSON_FIELD_SURGERIES = "surgeries";
    public static final String JSON_FIELD_HOSPITALIZATIONS = "hospitalizations";

    // Allergies Field Names
    public static final String JSON_FIELD_MEDICATIONS = "medications";
    public static final String JSON_FIELD_FOODS = "foods";
    public static final String JSON_FIELD_ENVIRONMENTAL = "environmental";
    public static final String JSON_FIELD_ROLE = "role";
    public static final String JSON_FIELD_STATUS = "status";
    public static final String JSON_FIELD_CREATED_AT = "createdAt";
    public static final String JSON_FIELD_UPDATED_AT = "updatedAt";
    public static final String JSON_FIELD_APPOINTMENTS = "appointments";
    public static final String JSON_FIELD_SUMMARY = "summary";
    public static final String JSON_FIELD_SUCCESS = "success";
    public static final String JSON_FIELD_MESSAGE = "message";
    public static final String JSON_FIELD_ERROR = "error";
    public static final String JSON_FIELD_USER_PROFILE = "userProfile";
    public static final String JSON_FIELD_PATIENT_PROFILE = "patientProfile";

    // Medical History Response Fields
    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_PROVIDER_ID = "providerId";
    public static final String JSON_FIELD_PROVIDER_NAME = "providerName";
    public static final String JSON_FIELD_SCHEDULED_AT = "scheduledAt";
    public static final String JSON_FIELD_APPOINTMENT_TYPE = "appointmentType";
    public static final String JSON_FIELD_MEDICAL_RECORDS = "medicalRecords";
    public static final String JSON_FIELD_RECORD_TYPE = "recordType";
    public static final String JSON_FIELD_CONTENT = "content";
    public static final String JSON_FIELD_IS_PATIENT_VISIBLE = "isPatientVisible";
    public static final String JSON_FIELD_RELEASE_DATE = "releaseDate";
    public static final String JSON_FIELD_TOTAL_APPOINTMENTS = "totalAppointments";
    public static final String JSON_FIELD_COMPLETED_APPOINTMENTS = "completedAppointments";
    public static final String JSON_FIELD_UPCOMING_APPOINTMENTS = "upcomingAppointments";
    public static final String JSON_FIELD_LAST_VISIT = "lastVisit";
}
