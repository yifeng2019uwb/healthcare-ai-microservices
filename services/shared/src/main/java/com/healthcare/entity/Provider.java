package com.healthcare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Provider entity representing healthcare provider information
 */
@Entity
@Table(name = "providers")
public class Provider extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 100)
    @Column(name = "specialty")
    private String specialty;

    @Size(max = 20)
    @Column(name = "license_number")
    private String licenseNumber;

    @Size(max = 100)
    @Column(name = "qualification")
    private String qualification;

    @Size(max = 2000)
    @Column(name = "bio")
    private String bio;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Size(max = 500)
    @Column(name = "office_address")
    private String officeAddress;

    @Size(max = 20)
    @Column(name = "office_phone")
    private String officePhone;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    // Constructors
    public Provider() {}

    public Provider(User user) {
        this.user = user;
        this.isAvailable = true;
    }

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getOfficeAddress() {
        return officeAddress;
    }

    public void setOfficeAddress(String officeAddress) {
        this.officeAddress = officeAddress;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}
