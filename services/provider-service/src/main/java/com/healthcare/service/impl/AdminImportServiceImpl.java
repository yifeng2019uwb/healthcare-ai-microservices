package com.healthcare.service.impl;

import com.healthcare.csv.SyntheaCsvParser;
import com.healthcare.csv.SyntheaRows;
import com.healthcare.dao.AllergyDao;
import com.healthcare.dao.ConditionDao;
import com.healthcare.dao.EncounterDao;
import com.healthcare.dao.OrganizationDao;
import com.healthcare.dao.PatientDao;
import com.healthcare.dao.ProviderDao;
import com.healthcare.dto.ImportResult;
import com.healthcare.entity.Allergy;
import com.healthcare.entity.AllergyId;
import com.healthcare.entity.Condition;
import com.healthcare.entity.ConditionId;
import com.healthcare.entity.Encounter;
import com.healthcare.entity.Organization;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Provider;
import com.healthcare.enums.Gender;
import com.healthcare.service.AdminImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminImportServiceImpl implements AdminImportService {

    private static final Logger log = LoggerFactory.getLogger(AdminImportServiceImpl.class);

    private final OrganizationDao organizationDao;
    private final PatientDao      patientDao;
    private final ProviderDao     providerDao;
    private final EncounterDao    encounterDao;
    private final ConditionDao    conditionDao;
    private final AllergyDao      allergyDao;

    public AdminImportServiceImpl(OrganizationDao organizationDao,
                                  PatientDao patientDao,
                                  ProviderDao providerDao,
                                  EncounterDao encounterDao,
                                  ConditionDao conditionDao,
                                  AllergyDao allergyDao) {
        this.organizationDao = organizationDao;
        this.patientDao      = patientDao;
        this.providerDao     = providerDao;
        this.encounterDao    = encounterDao;
        this.conditionDao    = conditionDao;
        this.allergyDao      = allergyDao;
    }

    @Override
    public ImportResult importOrganizations(MultipartFile file) {
        var rows = SyntheaCsvParser.parseOrganizations(file);
        if (rows.isEmpty()) return ImportResult.empty();

        List<SyntheaRows.Organization> valid = rows.stream()
                .filter(r -> r.id() != null && r.name() != null && !r.name().isBlank())
                .toList();
        int skippedInvalid = rows.size() - valid.size();

        Set<UUID> existing = existingIds(organizationDao,
                valid.stream().map(SyntheaRows.Organization::id).toList(),
                Organization::getId);
        List<Organization> toSave = valid.stream()
                .filter(r -> !existing.contains(r.id()))
                .map(this::toOrganization)
                .toList();
        organizationDao.saveAll(toSave);
        ImportResult result = new ImportResult(rows.size(), toSave.size(), existing.size(), skippedInvalid);
        log.info("Import organizations: total={}, imported={}, skippedDuplicate={}, skippedInvalid={}",
                result.total(), result.imported(), result.skippedDuplicate(), result.skippedInvalid());
        return result;
    }

    @Override
    public ImportResult importPatients(MultipartFile file) {
        var rows = SyntheaCsvParser.parsePatients(file);
        if (rows.isEmpty()) return ImportResult.empty();

        List<SyntheaRows.Patient> valid = rows.stream()
                .filter(r -> r.id() != null
                          && r.firstName() != null && !r.firstName().isBlank()
                          && r.lastName()  != null && !r.lastName().isBlank())
                .toList();
        int skippedInvalid = rows.size() - valid.size();

        Set<UUID> existing = existingIds(patientDao,
                valid.stream().map(SyntheaRows.Patient::id).toList(),
                Patient::getId);
        List<Patient> toSave = valid.stream()
                .filter(r -> !existing.contains(r.id()))
                .map(this::toPatient)
                .toList();
        patientDao.saveAll(toSave);
        ImportResult result = new ImportResult(rows.size(), toSave.size(), existing.size(), skippedInvalid);
        log.info("Import patients: total={}, imported={}, skippedDuplicate={}, skippedInvalid={}",
                result.total(), result.imported(), result.skippedDuplicate(), result.skippedInvalid());
        return result;
    }

    @Override
    public ImportResult importProviders(MultipartFile file) {
        var rows = SyntheaCsvParser.parseProviders(file);
        if (rows.isEmpty()) return ImportResult.empty();

        List<SyntheaRows.Provider> valid = rows.stream()
                .filter(r -> r.id() != null && r.organizationId() != null
                          && r.name() != null && !r.name().isBlank())
                .toList();
        int skippedInvalid = rows.size() - valid.size();

        Set<UUID> existing = existingIds(providerDao,
                valid.stream().map(SyntheaRows.Provider::id).toList(),
                Provider::getId);
        List<Provider> toSave = valid.stream()
                .filter(r -> !existing.contains(r.id()))
                .map(this::toProvider)
                .toList();
        providerDao.saveAll(toSave);
        ImportResult result = new ImportResult(rows.size(), toSave.size(), existing.size(), skippedInvalid);
        log.info("Import providers: total={}, imported={}, skippedDuplicate={}, skippedInvalid={}",
                result.total(), result.imported(), result.skippedDuplicate(), result.skippedInvalid());
        return result;
    }

    @Override
    public ImportResult importEncounters(MultipartFile file) {
        var rows = SyntheaCsvParser.parseEncounters(file);
        if (rows.isEmpty()) return ImportResult.empty();

        List<SyntheaRows.Encounter> valid = rows.stream()
                .filter(r -> r.id() != null && r.patientId() != null
                          && r.providerId() != null && r.startTime() != null)
                .toList();
        int skippedInvalid = rows.size() - valid.size();

        Set<UUID> existing = existingIds(encounterDao,
                valid.stream().map(SyntheaRows.Encounter::id).toList(),
                Encounter::getId);
        List<Encounter> toSave = valid.stream()
                .filter(r -> !existing.contains(r.id()))
                .map(this::toEncounter)
                .toList();
        encounterDao.saveAll(toSave);
        ImportResult result = new ImportResult(rows.size(), toSave.size(), existing.size(), skippedInvalid);
        log.info("Import encounters: total={}, imported={}, skippedDuplicate={}, skippedInvalid={}",
                result.total(), result.imported(), result.skippedDuplicate(), result.skippedInvalid());
        return result;
    }

    @Override
    public ImportResult importConditions(MultipartFile file) {
        var rows = SyntheaCsvParser.parseConditions(file);
        if (rows.isEmpty()) return ImportResult.empty();

        List<SyntheaRows.Condition> valid = rows.stream()
                .filter(r -> r.patientId() != null && r.encounterId() != null
                          && r.code() != null && !r.code().isBlank())
                .toList();
        int skippedInvalid = rows.size() - valid.size();

        List<ConditionId> ids = valid.stream()
                .map(r -> new ConditionId(r.patientId(), r.encounterId(), r.code()))
                .toList();
        Set<ConditionId> existing = conditionDao.findAllById(ids).stream()
                .map(Condition::getId).collect(Collectors.toSet());
        List<Condition> toSave = valid.stream()
                .filter(r -> !existing.contains(new ConditionId(r.patientId(), r.encounterId(), r.code())))
                .map(this::toCondition)
                .toList();
        conditionDao.saveAll(toSave);
        ImportResult result = new ImportResult(rows.size(), toSave.size(), existing.size(), skippedInvalid);
        log.info("Import conditions: total={}, imported={}, skippedDuplicate={}, skippedInvalid={}",
                result.total(), result.imported(), result.skippedDuplicate(), result.skippedInvalid());
        return result;
    }

    @Override
    public ImportResult importAllergies(MultipartFile file) {
        var rows = SyntheaCsvParser.parseAllergies(file);
        if (rows.isEmpty()) return ImportResult.empty();

        List<SyntheaRows.Allergy> valid = rows.stream()
                .filter(r -> r.patientId() != null && r.encounterId() != null
                          && r.code() != null && !r.code().isBlank())
                .toList();
        int skippedInvalid = rows.size() - valid.size();

        List<AllergyId> ids = valid.stream()
                .map(r -> new AllergyId(r.patientId(), r.encounterId(), r.code()))
                .toList();
        Set<AllergyId> existing = allergyDao.findAllById(ids).stream()
                .map(Allergy::getId).collect(Collectors.toSet());
        List<Allergy> toSave = valid.stream()
                .filter(r -> !existing.contains(new AllergyId(r.patientId(), r.encounterId(), r.code())))
                .map(this::toAllergy)
                .toList();
        allergyDao.saveAll(toSave);
        ImportResult result = new ImportResult(rows.size(), toSave.size(), existing.size(), skippedInvalid);
        log.info("Import allergies: total={}, imported={}, skippedDuplicate={}, skippedInvalid={}",
                result.total(), result.imported(), result.skippedDuplicate(), result.skippedInvalid());
        return result;
    }

    // -------------------------------------------------------------------------
    // Entity mappers
    // -------------------------------------------------------------------------

    private Organization toOrganization(SyntheaRows.Organization r) {
        Organization org = new Organization(r.name());
        org.setId(r.id());
        org.setAddress(r.address());
        org.setCity(r.city());
        org.setState(r.state());
        org.setZip(r.zip());
        org.setPhone(r.phone());
        org.setLat(r.lat());
        org.setLon(r.lon());
        org.setRevenue(r.revenue());
        org.setUtilization(r.utilization());
        return org;
    }

    private Patient toPatient(SyntheaRows.Patient r) {
        Patient patient = new Patient(r.firstName(), r.lastName());
        patient.setId(r.id());
        patient.setBirthdate(r.birthdate());
        patient.setDeathdate(r.deathdate());
        patient.setSsn(r.ssn());
        patient.setDrivers(r.drivers());
        patient.setPassport(r.passport());
        patient.setPrefix(r.prefix());
        patient.setMiddleName(r.middleName());
        patient.setSuffix(r.suffix());
        patient.setMaiden(r.maiden());
        patient.setMarital(r.marital());
        patient.setRace(r.race());
        patient.setEthnicity(r.ethnicity());
        patient.setGender(r.gender() != null ? Gender.valueOf(r.gender()) : null);
        patient.setBirthplace(r.birthplace());
        patient.setAddress(r.address());
        patient.setCity(r.city());
        patient.setState(r.state());
        patient.setCounty(r.county());
        patient.setFips(r.fips());
        patient.setZip(r.zip());
        patient.setLat(r.lat());
        patient.setLon(r.lon());
        patient.setHealthcareExpenses(r.healthcareExpenses());
        patient.setHealthcareCoverage(r.healthcareCoverage());
        patient.setIncome(r.income());
        return patient;
    }

    private Provider toProvider(SyntheaRows.Provider r) {
        Provider provider = new Provider(r.organizationId(), r.name());
        provider.setId(r.id());
        provider.setGender(r.gender() != null ? Gender.valueOf(r.gender()) : null);
        provider.setSpeciality(r.speciality());
        provider.setEncounters(r.encounters());
        provider.setProcedures(r.procedures());
        return provider;
    }

    private Encounter toEncounter(SyntheaRows.Encounter r) {
        Encounter enc = new Encounter(r.providerId(), r.startTime());
        enc.setId(r.id());
        enc.setPatientId(r.patientId());
        enc.setOrganizationId(r.organizationId());
        enc.setPayerId(r.payerId());
        enc.setStopTime(r.stopTime());
        enc.setEncounterClass(r.encounterClass());
        enc.setCode(r.code());
        enc.setDescription(r.description());
        enc.setBaseCost(r.baseCost());
        enc.setTotalCost(r.totalCost());
        enc.setPayerCoverage(r.payerCoverage());
        enc.setReasonCode(r.reasonCode());
        enc.setReasonDesc(r.reasonDesc());
        return enc;
    }

    private Condition toCondition(SyntheaRows.Condition r) {
        Condition condition = new Condition(
                new ConditionId(r.patientId(), r.encounterId(), r.code()),
                r.startDate());
        condition.setStopDate(r.stopDate());
        condition.setSystem(r.system());
        condition.setDescription(r.description());
        return condition;
    }

    private Allergy toAllergy(SyntheaRows.Allergy r) {
        Allergy allergy = new Allergy(
                new AllergyId(r.patientId(), r.encounterId(), r.code()),
                r.startDate());
        allergy.setStopDate(r.stopDate());
        allergy.setSystem(r.system());
        allergy.setDescription(r.description());
        allergy.setAllergyType(r.allergyType());
        allergy.setCategory(r.category());
        allergy.setReaction1(r.reaction1());
        allergy.setDescription1(r.description1());
        allergy.setSeverity1(r.severity1());
        allergy.setReaction2(r.reaction2());
        allergy.setDescription2(r.description2());
        allergy.setSeverity2(r.severity2());
        return allergy;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private <E> Set<UUID> existingIds(JpaRepository<E, UUID> dao,
                                       List<UUID> ids,
                                       Function<E, UUID> getId) {
        if (ids.isEmpty()) return Set.of();
        return dao.findAllById(ids).stream()
                .map(getId)
                .collect(Collectors.toSet());
    }
}
