package com.healthcare.entity;

import com.healthcare.constants.AppConstants;
import com.healthcare.constants.DatabaseConstants;
import com.healthcare.enums.UserRole;
import com.healthcare.exception.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * User entity mapping to the users table.
 *
 * Stores authentication credentials and role assignment only.
 * NOT PHI — no patient demographic data here.
 * Only auth-service has DB access to this table (enforced via permissions.sql).
 *
 * Demographic data lives in patients / providers tables.
 */
@Entity
@Table(name = DatabaseConstants.TABLE_USERS,
       indexes = {
           @Index(name = DatabaseConstants.INDEX_USERS_EMAIL,    columnList = DatabaseConstants.COL_EMAIL),
           @Index(name = DatabaseConstants.INDEX_USERS_USERNAME, columnList = DatabaseConstants.COL_USERNAME),
           @Index(name = DatabaseConstants.INDEX_USERS_ROLE,     columnList = DatabaseConstants.COL_ROLE),
           @Index(name = DatabaseConstants.INDEX_USERS_FHIR_ID,  columnList = DatabaseConstants.COL_FHIR_ID)
       })
public class User extends BaseEntity {
    private static final String FIELD_USERNAME      = "Username";
    private static final String FIELD_EMAIL         = "Email";
    private static final String FIELD_PASSWORD_HASH = "Password hash";
    private static final String FIELD_ROLE          = "Role";

    // ------------------------------------------------------------------
    // Identity
    // ------------------------------------------------------------------

    @Id
    @Column(name = DatabaseConstants.COL_ID, updatable = false, nullable = false,
            columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Size(max = DatabaseConstants.LEN_USERNAME)
    @Column(name = DatabaseConstants.COL_USERNAME, unique = true)
    private String username;

    @Email
    @Size(max = DatabaseConstants.LEN_EMAIL)
    @Column(name = DatabaseConstants.COL_EMAIL, unique = true)
    private String email;

    /** BCrypt hashed — never store or return plain text password. */
    @Size(max = DatabaseConstants.LEN_PASSWORD_HASH)
    @Column(name = DatabaseConstants.COL_PASSWORD_HASH)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_ROLE, length = DatabaseConstants.LEN_ROLE)
    private UserRole role;

    @Column(name = DatabaseConstants.COL_IS_ACTIVE)
    private Boolean isActive = true;

    /** FHIR resource ID — patients.id or providers.id linked at registration. Null for ADMIN. */
    @Column(name = DatabaseConstants.COL_FHIR_ID)
    private UUID fhirId;

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    /** For JPA only. */
    @SuppressWarnings(AppConstants.SUPPRESS_UNUSED)
    protected User() {}

    /**
     * Create a new user account.
     *
     * @param username     unique username
     * @param email        unique email
     * @param passwordHash BCrypt hashed password — never raw
     * @param role         PATIENT, PROVIDER, or ADMIN
     */
    public User(String username, String email, String passwordHash, UserRole role) {
        if (username == null || username.isBlank())
            throw new ValidationException(FIELD_USERNAME + " is required");
        if (email == null || email.isBlank())
            throw new ValidationException(FIELD_EMAIL + " is required");
        if (passwordHash == null || passwordHash.isBlank())
            throw new ValidationException(FIELD_PASSWORD_HASH + " cannot be blank");
        if (role == null)
            throw new ValidationException(FIELD_ROLE + " is required");

        this.id           = UUID.randomUUID();
        this.username     = username.trim();
        this.email        = email.trim();
        this.passwordHash = passwordHash;
        this.role         = role;
        this.isActive     = true;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public UUID getId()             { return id; }
    public void setId(UUID id) {
        if (this.id != null) throw new ValidationException("ID is already set and cannot be changed");
        this.id = id;
    }

    public String getUsername()     { return username; }
    public String getEmail()        { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole()       { return role; }
    public Boolean getIsActive()    { return isActive; }
    public UUID getFhirId()         { return fhirId; }

    // ------------------------------------------------------------------
    // Setters
    // username and email are immutable after creation
    // ------------------------------------------------------------------

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank())
            throw new ValidationException(FIELD_PASSWORD_HASH + " cannot be blank");
        this.passwordHash = passwordHash;
    }

    public void setIsActive(Boolean isActive) {
        if (isActive == null)
            throw new ValidationException("isActive cannot be null");
        this.isActive = isActive;
    }

    public void setFhirId(UUID fhirId) {
        this.fhirId = fhirId;
    }

    // ------------------------------------------------------------------
    // Business methods
    // ------------------------------------------------------------------

    public boolean isActive()    { return Boolean.TRUE.equals(isActive); }
    public boolean isPatient()   { return UserRole.PATIENT == role; }
    public boolean isProvider()  { return UserRole.PROVIDER == role; }
}