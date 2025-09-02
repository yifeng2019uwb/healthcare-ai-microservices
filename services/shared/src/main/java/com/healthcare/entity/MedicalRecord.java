package com.healthcare.entity;

import com.healthcare.enums.MedicalRecordType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Medical record entity representing patient medical records
 */
@Entity
@Table(name = "medical_records")
public class MedicalRecord extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private MedicalRecordType recordType;

    @NotBlank
    @Size(max = 200)
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 2000)
    @Column(name = "description")
    private String description;

    @Column(name = "record_date", nullable = false)
    private LocalDateTime recordDate;

    @Size(max = 2000)
    @Column(name = "diagnosis")
    private String diagnosis;

    @Size(max = 2000)
    @Column(name = "treatment")
    private String treatment;

    @Size(max = 2000)
    @Column(name = "medications")
    private String medications;

    @Size(max = 2000)
    @Column(name = "vital_signs")
    private String vitalSigns;

    @Size(max = 2000)
    @Column(name = "file_urls")
    private String fileUrls; // JSON string of file URLs

    // Constructors
    public MedicalRecord() {}

    public MedicalRecord(Patient patient, Provider provider, MedicalRecordType recordType, String title) {
        this.patient = patient;
        this.provider = provider;
        this.recordType = recordType;
        this.title = title;
        this.recordDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public MedicalRecordType getRecordType() {
        return recordType;
    }

    public void setRecordType(MedicalRecordType recordType) {
        this.recordType = recordType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDateTime recordDate) {
        this.recordDate = recordDate;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(String vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public String getFileUrls() {
        return fileUrls;
    }

    public void setFileUrls(String fileUrls) {
        this.fileUrls = fileUrls;
    }
}
