package com.healthcare.entity;

import com.healthcare.constants.AppConstants;
import com.healthcare.constants.DatabaseConstants;
import com.healthcare.constants.ValidationConstants;
import com.healthcare.constants.ValidationPatterns;
import com.healthcare.enums.Gender;
import com.healthcare.exception.ValidationException;
import com.healthcare.utils.ValidationUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


/**
 * Patient entity mapping to the patients table.
 *
 * Contains Synthea synthetic patient data fields plus application fields.
 *
 * Registration flow:
 *   1. Patient record exists (created by provider)
 *   2. Patient registers account using MRN + first_name + last_name
 *   3. On successful match, auth_id is linked to users.id
 *
 * auth_id is null until the patient registers an account.
 */
@Entity
@Table(name = DatabaseConstants.TABLE_PATIENTS,
       indexes = {
           @Index(name = DatabaseConstants.INDEX_PATIENTS_AUTH_ID,
                  columnList = DatabaseConstants.COL_AUTH_ID),
           @Index(name = DatabaseConstants.INDEX_PATIENTS_MRN,
                  columnList = DatabaseConstants.COL_MRN),
           @Index(name = DatabaseConstants.INDEX_PATIENTS_NAME,
                  columnList = DatabaseConstants.COL_LAST_NAME + "," + DatabaseConstants.COL_FIRST_NAME)
       })
public class Patient extends ProfileBaseEntity {
    private static final String FIELD_FIRST_NAME = "First name";
    private static final String FIELD_LAST_NAME  = "Last name";
    private static final String FIELD_MIDDLE_NAME  = "Middle name";
    private static final String FIELD_PHONE = "Phone";

    // ------------------------------------------------------------------
    // Synthea fields
    // ------------------------------------------------------------------

    @Column(name = DatabaseConstants.COL_BIRTHDATE)
    private LocalDate birthdate;

    @Column(name = DatabaseConstants.COL_DEATHDATE)
    private LocalDate deathdate;

    @Size(max = DatabaseConstants.LEN_SSN)
    @Column(name = DatabaseConstants.COL_SSN)
    private String ssn;

    @Size(max = DatabaseConstants.LEN_DRIVERS)
    @Column(name = DatabaseConstants.COL_DRIVERS)
    private String drivers;

    @Size(max = DatabaseConstants.LEN_PASSPORT)
    @Column(name = DatabaseConstants.COL_PASSPORT)
    private String passport;

    @Size(max = DatabaseConstants.LEN_PREFIX)
    @Column(name = DatabaseConstants.COL_PREFIX)
    private String prefix;

    @Size(max = DatabaseConstants.LEN_FIRST_NAME)
    @Column(name = DatabaseConstants.COL_FIRST_NAME)
    private String firstName;

    @Size(max = DatabaseConstants.LEN_MIDDLE_NAME)
    @Column(name = DatabaseConstants.COL_MIDDLE_NAME)
    private String middleName;

    @Size(max = DatabaseConstants.LEN_MIDDLE_NAME)
    @Column(name = DatabaseConstants.COL_LAST_NAME)
    private String lastName;

    @Size(max = DatabaseConstants.LEN_SUFFIX)
    @Column(name = DatabaseConstants.COL_SUFFIX)
    private String suffix;

    @Size(max = DatabaseConstants.LEN_MAIDEN)
    @Column(name = DatabaseConstants.COL_MAIDEN)
    private String maiden;

    @Size(max = DatabaseConstants.LEN_MARITAL)
    @Column(name = DatabaseConstants.COL_MARITAL)
    private String marital;

    @Size(max = DatabaseConstants.LEN_RACE)
    @Column(name = DatabaseConstants.COL_RACE)
    private String race;

    @Size(max = DatabaseConstants.LEN_ETHNICITY)
    @Column(name = DatabaseConstants.COL_ETHNICITY)
    private String ethnicity;

    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_GENDER)
    private Gender gender;

    @Size(max = DatabaseConstants.LEN_BIRTHPLACE)
    @Column(name = DatabaseConstants.COL_BIRTHPLACE)
    private String birthplace;

    @Size(max = DatabaseConstants.LEN_ADDRESS)
    @Column(name = DatabaseConstants.COL_ADDRESS)
    private String address;

    @Size(max = DatabaseConstants.LEN_CITY)
    @Column(name = DatabaseConstants.COL_CITY)
    private String city;

    @Size(max = DatabaseConstants.LEN_STATE)
    @Column(name = DatabaseConstants.COL_STATE)
    private String state;

    @Size(max = DatabaseConstants.LEN_COUNTY)
    @Column(name = DatabaseConstants.COL_COUNTY)
    private String county;

    @Size(max = DatabaseConstants.LEN_FIPS)
    @Column(name = DatabaseConstants.COL_FIPS)
    private String fips;

    @Size(max = DatabaseConstants.LEN_ZIP)
    @Column(name = DatabaseConstants.COL_ZIP)
    private String zip;

    @Column(name = DatabaseConstants.COL_LAT,
        precision = DatabaseConstants.LEN_LAT_LON_PRECISION,
        scale = DatabaseConstants.LEN_LAT_LON_SCALE
    )
    private BigDecimal lat;

    @Column(name = DatabaseConstants.COL_LON,
        precision = DatabaseConstants.LEN_LAT_LON_PRECISION,
        scale = DatabaseConstants.LEN_LAT_LON_SCALE
    )
    private BigDecimal lon;

    @Column(name = DatabaseConstants.COL_HEALTHCARE_EXPENSES,
        precision = DatabaseConstants.LEN_HEALTHCARE_PRECISION,
        scale = DatabaseConstants.LEN_MONEY_SCALE
    )
    private BigDecimal healthcareExpenses;

    @Column(name = DatabaseConstants.COL_HEALTHCARE_COVERAGE,
        precision = DatabaseConstants.LEN_HEALTHCARE_PRECISION,
        scale = DatabaseConstants.LEN_MONEY_SCALE
    )
    private BigDecimal healthcareCoverage;

    @Column(name = DatabaseConstants.COL_INCOME)
    private Integer income;

    // ------------------------------------------------------------------
    // Application fields
    // ------------------------------------------------------------------

    /**
     * Links to users.id after patient registers an account.
     * Null until registration is complete.
     */
    @Column(name = DatabaseConstants.COL_AUTH_ID, unique = true)
    private UUID authId;

    /**
     * Medical Record Number — auto-generated by DB sequence.
     * Format: MRN-000001.
     * Used for patient registration: MRN + first_name + last_name must match.
     */
    @Size(max = DatabaseConstants.LEN_MRN)
    @Column(name = DatabaseConstants.COL_MRN, unique = true)
    private String mrn;

    @Size(max = DatabaseConstants.LEN_PHONE)
    @Pattern(regexp = ValidationPatterns.PHONE,
             message = "Phone must be a valid international format")
    @Column(name = DatabaseConstants.COL_PHONE)
    private String phone;

    @Size(max = DatabaseConstants.LEN_EMERGENCY_CONTACT)
    @Column(name = DatabaseConstants.COL_EMERGENCY_CONTACT)
    private String emergencyContact;

    @Size(max = DatabaseConstants.LEN_BLOOD_TYPE)
    @Column(name = DatabaseConstants.COL_BLOOD_TYPE)
    private String bloodType;

    @Size(max = DatabaseConstants.LEN_NOTES)
    @Column(name = DatabaseConstants.COL_NOTES,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TEXT)
    private String notes;

    // ------------------------------------------------------------------
    // JPA relationship
    // ------------------------------------------------------------------

    /**
     * Navigation to the linked user account.
     * Null until patient completes registration.
     * insertable/updatable = false — authId column manages the FK.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COL_AUTH_ID,
                insertable = false, updatable = false)
    private User user;

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    /** For JPA only. */
    @SuppressWarnings(AppConstants.SUPPRESS_UNUSED)
    private Patient() {}

    /**
     * Minimal constructor for API-created patient records.
     * firstName and lastName required for MRN-based registration validation.
     *
     * @param firstName patient first name
     * @param lastName  patient last name
     */
    public Patient(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank())
            throw new ValidationException(FIELD_FIRST_NAME + " is required");
        if (lastName == null || lastName.isBlank())
            throw new ValidationException(FIELD_LAST_NAME + " is required");

        this.firstName = firstName;
        this.lastName  = lastName;
    }

    // ------------------------------------------------------------------
    // Business methods
    // ------------------------------------------------------------------

    /**
     * Whether this patient has registered an account.
     */
    public boolean isRegistered() {
        return authId != null;
    }

    /**
     * Link this patient record to a user account after successful registration.
     * Can only be set once — cannot re-link to a different user.
     *
     * @param userId the UUID from users.id
     */
    public void linkAuthAccount(UUID userId) {
        if (userId == null)
            throw new ValidationException("User ID cannot be null");
        if (this.authId != null)
            throw new ValidationException("Patient is already linked to an account");
        this.authId = userId;
    }

    /**
     * Validate registration credentials.
     * Patient must provide MRN + first_name + last_name to register.
     *
     * @param mrn       provided MRN
     * @param firstName provided first name
     * @param lastName  provided last name
     * @return true if all three match this patient record
     */
    public boolean matchesRegistrationCredentials(String mrn, String firstName, String lastName) {
        if (mrn == null || firstName == null || lastName == null)
            return false;
        return this.mrn.equalsIgnoreCase(mrn)
                && this.firstName.equalsIgnoreCase(firstName.trim())
                && this.lastName.equalsIgnoreCase(lastName.trim());
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public LocalDate getBirthdate()              { return birthdate; }
    public LocalDate getDeathdate()              { return deathdate; }
    public String getSsn()                       { return ssn; }
    public String getDrivers()                   { return drivers; }
    public String getPassport()                  { return passport; }
    public String getPrefix()                    { return prefix; }
    public String getFirstName()                 { return firstName; }
    public String getMiddleName()                { return middleName; }
    public String getLastName()                  { return lastName; }
    public String getSuffix()                    { return suffix; }
    public String getMaiden()                    { return maiden; }
    public String getMarital()                   { return marital; }
    public String getRace()                      { return race; }
    public String getEthnicity()                 { return ethnicity; }
    public Gender getGender()                    { return gender; }
    public String getBirthplace()                { return birthplace; }
    public String getAddress()                   { return address; }
    public String getCity()                      { return city; }
    public String getState()                     { return state; }
    public String getCounty()                    { return county; }
    public String getFips()                      { return fips; }
    public String getZip()                       { return zip; }
    public BigDecimal getLat()                   { return lat; }
    public BigDecimal getLon()                   { return lon; }
    public BigDecimal getHealthcareExpenses()    { return healthcareExpenses; }
    public BigDecimal getHealthcareCoverage()    { return healthcareCoverage; }
    public Integer getIncome()                   { return income; }
    public UUID getAuthId()                      { return authId; }
    public String getMrn()                       { return mrn; }
    public String getPhone()                     { return phone; }
    public String getEmergencyContact()          { return emergencyContact; }
    public String getBloodType()                 { return bloodType; }
    public String getNotes()                     { return notes; }
    public User getUser()                        { return user; }
    public String getFullName()                  { return firstName + " " + lastName; }

    // ------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------

    // authId — set only via linkAuthAccount(), no direct setter
    // mrn    — set by DB sequence, no setter

    public void setBirthdate(LocalDate birthdate)       { this.birthdate = birthdate; }
    public void setDeathdate(LocalDate deathdate)       { this.deathdate = deathdate; }
    public void setSsn(String ssn)                      { this.ssn = ssn; }
    public void setDrivers(String drivers)              { this.drivers = drivers; }
    public void setPassport(String passport)            { this.passport = passport; }
    public void setPrefix(String prefix)                { this.prefix = prefix; }

    public void setFirstName(String firstName) {
        this.firstName = ValidationUtils.validateAndNormalizeString(
                firstName, FIELD_FIRST_NAME, ValidationConstants.MAX_NAME_LENGTH);
    }

    public void setMiddleName(String middleName) {
        this.middleName = ValidationUtils.validateAndNormalizeString(
                middleName, FIELD_MIDDLE_NAME, ValidationConstants.MAX_NAME_LENGTH);
    }

    public void setLastName(String lastName) {
        this.lastName = ValidationUtils.validateAndNormalizeString(
                lastName, FIELD_LAST_NAME, ValidationConstants.MAX_NAME_LENGTH);
    }

    public void setSuffix(String suffix)                { this.suffix = suffix; }
    public void setMaiden(String maiden)                { this.maiden = maiden; }
    public void setMarital(String marital)              { this.marital = marital; }
    public void setRace(String race)                    { this.race = race; }
    public void setEthnicity(String ethnicity)          { this.ethnicity = ethnicity; }
    public void setGender(Gender gender)                { this.gender = gender; }
    public void setBirthplace(String birthplace)        { this.birthplace = birthplace; }
    public void setAddress(String address)              { this.address = address; }
    public void setCity(String city)                    { this.city = city; }
    public void setState(String state)                  { this.state = state; }
    public void setCounty(String county)                { this.county = county; }
    public void setFips(String fips)                    { this.fips = fips; }
    public void setZip(String zip)                      { this.zip = zip; }
    public void setLat(BigDecimal lat)                  { this.lat = lat; }
    public void setLon(BigDecimal lon)                  { this.lon = lon; }

    public void setHealthcareExpenses(BigDecimal healthcareExpenses) {
        this.healthcareExpenses = healthcareExpenses;
    }

    public void setHealthcareCoverage(BigDecimal healthcareCoverage) {
        this.healthcareCoverage = healthcareCoverage;
    }

    public void setIncome(Integer income)               { this.income = income; }

    public void setPhone(String phone) {
        this.phone = ValidationUtils.validateAndNormalizeString(
                phone, FIELD_PHONE, ValidationConstants.MAX_PHONE_LENGTH,
                ValidationPatterns.PHONE,
                FIELD_PHONE + " must be a valid international format");
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = ValidationUtils.validateAndNormalizeString(
                emergencyContact, "Emergency contact", DatabaseConstants.LEN_EMERGENCY_CONTACT);
    }

    public void setBloodType(String bloodType)          { this.bloodType = bloodType; }
    public void setNotes(String notes)                  { this.notes = notes; }
}