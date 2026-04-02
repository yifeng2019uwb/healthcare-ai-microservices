package com.healthcare.entity;

import com.healthcare.constants.AppConstants;
import com.healthcare.constants.DatabaseConstants;
import com.healthcare.exception.ValidationException;
import com.healthcare.utils.ValidationUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Organization entity mapping to the organizations table.
 * Represents healthcare organizations that providers belong to.
 */
@Entity
@Table(name = DatabaseConstants.TABLE_ORGANIZATIONS,
       indexes = {
           @Index(name = DatabaseConstants.INDEX_ORGANIZATIONS_NAME,
                  columnList = DatabaseConstants.COL_NAME)
       })
public class Organization extends ProfileBaseEntity {
    private static final String FIELD_NAME = "Name";
    private static final String FIELD_ORGANIZATION_NAME = "Organization Name";
    private static final String FIELD_ADRESS = "Address";
    private static final String FIELD_STATE = "state";
    private static final String FIELD_CITY = "city";


    @Size(max = DatabaseConstants.LEN_ORGANIZATION_NAME)
    @Column(name = DatabaseConstants.COL_NAME)
    private String name;

    @Size(max = DatabaseConstants.LEN_ADDRESS)
    @Column(name = DatabaseConstants.COL_ADDRESS)
    private String address;

    @Size(max = DatabaseConstants.LEN_CITY)
    @Column(name = DatabaseConstants.COL_CITY)
    private String city;

    @Size(max = DatabaseConstants.LEN_STATE)
    @Column(name = DatabaseConstants.COL_STATE)
    private String state;

    @Size(max = DatabaseConstants.LEN_ZIP)
    @Column(name = DatabaseConstants.COL_ZIP)
    private String zip;

    @Size(max = DatabaseConstants.LEN_PHONE)
    @Column(name = DatabaseConstants.COL_PHONE)
    private String phone;

    @Column(name = DatabaseConstants.COL_LAT,
        precision = DatabaseConstants.LEN_LAT_LON_PRECISION,
         scale = DatabaseConstants.LEN_LAT_LON_SCALE)
    private BigDecimal lat;

    @Column(name = DatabaseConstants.COL_LON,
        precision = DatabaseConstants.LEN_LAT_LON_PRECISION,
        scale = DatabaseConstants.LEN_LAT_LON_SCALE)
    private BigDecimal lon;

    @Column(name = DatabaseConstants.COL_REVENUE,
        precision = DatabaseConstants.LEN_HEALTHCARE_PRECISION,
        scale = DatabaseConstants.LEN_MONEY_SCALE)
    private BigDecimal revenue;

    @Column(name = DatabaseConstants.COL_UTILIZATION)
    private Integer utilization;

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    /** For JPA only. */
    @SuppressWarnings(AppConstants.SUPPRESS_UNUSED)
    protected Organization() {}

    /**
     * Create a new organization.
     *
     * @param name organization name
     */
    public Organization(String name) {
        if (name == null || name.isBlank())
            throw new ValidationException(FIELD_NAME + " is required");
        this.name = name.trim();
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public String getName()          { return name; }
    public String getAddress()       { return address; }
    public String getCity()          { return city; }
    public String getState()         { return state; }
    public String getZip()           { return zip; }
    public String getPhone()         { return phone; }
    public BigDecimal getLat()       { return lat; }
    public BigDecimal getLon()       { return lon; }
    public BigDecimal getRevenue()   { return revenue; }
    public Integer getUtilization()  { return utilization; }

    // ------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------

    public void setName(String name) {
        this.name = ValidationUtils.validateAndNormalizeString(
            name, FIELD_ORGANIZATION_NAME, DatabaseConstants.LEN_ORGANIZATION_NAME);
    }

    public void setAddress(String address) {
        this.address = ValidationUtils.validateAndNormalizeString(
                address, FIELD_ADRESS, DatabaseConstants.LEN_ADDRESS);
    }

    public void setCity(String city) {
        this.city = ValidationUtils.validateAndNormalizeString(
                city, FIELD_CITY, DatabaseConstants.LEN_CITY);
    }

    public void setState(String state) {
        this.state = ValidationUtils.validateAndNormalizeString(
                state, FIELD_STATE, DatabaseConstants.LEN_STATE);
    }

    public void setZip(String zip)           { this.zip = zip; }
    public void setPhone(String phone)       { this.phone = phone; }
    public void setLat(BigDecimal lat)       { this.lat = lat; }
    public void setLon(BigDecimal lon)       { this.lon = lon; }
    public void setRevenue(BigDecimal revenue)     { this.revenue = revenue; }
    public void setUtilization(Integer utilization) { this.utilization = utilization; }
}