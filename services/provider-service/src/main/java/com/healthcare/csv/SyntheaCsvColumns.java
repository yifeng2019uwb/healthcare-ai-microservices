package com.healthcare.csv;

final class SyntheaCsvColumns {

    private SyntheaCsvColumns() {}

    // Shared
    public static final String ID          = "Id";
    public static final String NAME        = "NAME";
    public static final String ADDRESS     = "ADDRESS";
    public static final String CITY        = "CITY";
    public static final String STATE       = "STATE";
    public static final String ZIP         = "ZIP";
    public static final String LAT         = "LAT";
    public static final String LON         = "LON";
    public static final String PHONE       = "PHONE";
    public static final String GENDER      = "GENDER";
    public static final String CODE        = "CODE";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String SYSTEM      = "SYSTEM";
    public static final String START       = "START";
    public static final String STOP        = "STOP";
    public static final String PATIENT     = "PATIENT";
    public static final String ENCOUNTER   = "ENCOUNTER";

    // organizations.csv
    public static final String REVENUE     = "REVENUE";
    public static final String UTILIZATION = "UTILIZATION";

    // patients.csv
    public static final String BIRTHDATE            = "BIRTHDATE";
    public static final String DEATHDATE            = "DEATHDATE";
    public static final String SSN                  = "SSN";
    public static final String DRIVERS              = "DRIVERS";
    public static final String PASSPORT             = "PASSPORT";
    public static final String PREFIX               = "PREFIX";
    public static final String FIRST                = "FIRST";
    public static final String MIDDLE               = "MIDDLE";
    public static final String LAST                 = "LAST";
    public static final String SUFFIX               = "SUFFIX";
    public static final String MAIDEN               = "MAIDEN";
    public static final String MARITAL              = "MARITAL";
    public static final String RACE                 = "RACE";
    public static final String ETHNICITY            = "ETHNICITY";
    public static final String BIRTHPLACE           = "BIRTHPLACE";
    public static final String COUNTY               = "COUNTY";
    public static final String FIPS                 = "FIPS";
    public static final String HEALTHCARE_EXPENSES  = "HEALTHCARE_EXPENSES";
    public static final String HEALTHCARE_COVERAGE  = "HEALTHCARE_COVERAGE";
    public static final String INCOME               = "INCOME";

    // providers.csv
    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String SPECIALITY   = "SPECIALITY";
    public static final String ENCOUNTERS   = "ENCOUNTERS";
    public static final String PROCEDURES   = "PROCEDURES";

    // encounters.csv
    public static final String PROVIDER            = "PROVIDER";
    public static final String PAYER               = "PAYER";
    public static final String ENCOUNTERCLASS      = "ENCOUNTERCLASS";
    public static final String BASE_ENCOUNTER_COST = "BASE_ENCOUNTER_COST";
    public static final String TOTAL_CLAIM_COST    = "TOTAL_CLAIM_COST";
    public static final String PAYER_COVERAGE      = "PAYER_COVERAGE";
    public static final String REASONCODE          = "REASONCODE";
    public static final String REASONDESCRIPTION   = "REASONDESCRIPTION";

    // allergies.csv
    public static final String TYPE         = "TYPE";
    public static final String CATEGORY     = "CATEGORY";
    public static final String REACTION1    = "REACTION1";
    public static final String DESCRIPTION1 = "DESCRIPTION1";
    public static final String SEVERITY1    = "SEVERITY1";
    public static final String REACTION2    = "REACTION2";
    public static final String DESCRIPTION2 = "DESCRIPTION2";
    public static final String SEVERITY2    = "SEVERITY2";
}
