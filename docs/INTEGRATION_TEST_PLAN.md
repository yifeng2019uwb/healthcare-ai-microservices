# Integration Test Plan - Local Database Testing

> **Quick Plan**: Essential integration tests for local DB testing (not comprehensive)

## 🎯 **Goal**
Test critical service endpoints against local PostgreSQL database (`localhost:54322`) to verify end-to-end functionality.

## 📋 **Test Strategy**

### **Approach**
- **Local DB**: Connect to existing PostgreSQL on `localhost:54322`
- **Test Profile**: Use `application-test.yml` with test database config
- **Test Data**: Create via API endpoints (POST requests)
- **Cleanup**: **Skip cleanup** - HIPAA compliant (no DELETE for patient data)
- **Isolation**: Use unique test data (UUIDs/timestamps) to avoid conflicts

### **Test Scope** (Essential Only)
Focus on **3 critical paths**:
1. ✅ Patient Creation Flow
2. ✅ Patient Profile Retrieval
3. ✅ Health Check Endpoint

## 🧪 **Integration Tests to Implement**

### **1. Patient Service Integration Tests** ⭐ Priority

#### **1.1 PatientCreationIntegrationTest**
**Purpose**: Verify patient creation works end-to-end with DB

**Tests**:
- `testCreatePatient_Success()` - Valid patient creation
- `testCreatePatient_DuplicateEmail()` - Conflict handling
- `testCreatePatient_InvalidData()` - Validation errors

**Test Data**:
- Valid patient: email, firstName, lastName, phone
- Duplicate email for conflict test
- Invalid email format for validation test

**Location**: `services/patient-service/src/test/java/com/healthcare/controller/PatientCreationIntegrationTest.java`

#### **1.2 PatientProfileIntegrationTest**
**Purpose**: Verify patient profile retrieval from DB

**Tests**:
- `testGetPatientProfile_Success()` - Retrieve existing patient
- `testGetPatientProfile_NotFound()` - 404 handling
- `testGetPatientProfile_ByExternalUserId()` - Query by external ID

**Test Data**:
- Pre-created patient via API (POST /api/patients) with unique data
- Non-existent externalUserId for 404 test
- No cleanup needed (HIPAA compliant)

**Location**: `services/patient-service/src/test/java/com/healthcare/controller/PatientProfileIntegrationTest.java`

### **2. Health Check Integration Test** ⚡ Quick Win

#### **2.1 HealthCheckIntegrationTest**
**Purpose**: Verify service health and DB connectivity

**Tests**:
- `testHealthCheck_Success()` - Returns 200 OK
- `testHealthCheck_IncludesDbStatus()` - Checks DB connection

**Location**: `services/patient-service/src/test/java/com/healthcare/controller/HealthCheckIntegrationTest.java`

## 🔧 **Implementation Setup**

### **Required Dependencies**
- Spring Boot Test (already in pom.xml)
- PostgreSQL driver (already in pom.xml)

### **Test Configuration File**
**File**: `services/patient-service/src/test/resources/application-test.yml`
- Configure test database connection (`localhost:54322`)
- Use `test` profile
- Set JPA `ddl-auto: validate` (use existing schema)

### **Utility Classes to Create**

#### **1. ApiUrlUtils.java**
- Centralize API endpoint URLs
- Methods: `getCreatePatientUrl()`, `getProfileUrl()`, `getHealthUrl()`, etc.
- Use constants or base URL + endpoint paths

#### **2. TestDataBuilder.java**
- Helper functions for test input generation
- Methods: `createTestPatientInput()`, `createUniqueEmail()`, `createUniqueExternalUserId()`, etc.
- Generate unique test data (UUIDs/timestamps)

## 🧹 **Integration Test Cleanup Strategy**

### **No Cleanup - HIPAA Compliant**
**Important**: Patient data cannot be deleted per HIPAA regulations. Integration tests will **skip cleanup**.

**Approach**: Use **unique test data** (UUIDs/timestamps) to avoid conflicts between test runs.

### **Why No Cleanup?**
- ✅ **HIPAA Compliance**: Patient data must not be deleted
- ✅ **Simpler Tests**: No cleanup code needed
- ✅ **Unique Data**: UUIDs/timestamps prevent test conflicts
- ✅ **Realistic**: Matches production behavior (data retention)

## 📁 **File Structure**

```
services/patient-service/src/test/
├── java/com/healthcare/
│   ├── controller/
│   │   ├── PatientCreationIntegrationTest.java    ⭐
│   │   ├── PatientProfileIntegrationTest.java     ⭐
│   │   └── HealthCheckIntegrationTest.java        ⚡
│   ├── util/
│   │   ├── ApiUrlUtils.java                       (API endpoint URLs)
│   │   └── TestDataBuilder.java                   (Test input builders)
│   └── BaseIntegrationTest.java                   (Base test class)
├── resources/
│   └── application-test.yml                       (test DB config)
```

## 🚀 **Execution Plan**

### **Phase 1: Quick Setup**
1. Create `application-test.yml` with test DB config
2. Create utility classes:
   - `ApiUrlUtils.java` - Centralize API endpoint URLs
   - `TestDataBuilder.java` - Helper functions for test input generation
3. Create `BaseIntegrationTest` base class
4. Implement `HealthCheckIntegrationTest` (simplest first - no cleanup needed)

### **Phase 2: Core Tests**
5. Implement `PatientCreationIntegrationTest` with 3 test cases
   - Use `ApiUrlUtils` for endpoints
   - Use `TestDataBuilder` for test input
6. Implement `PatientProfileIntegrationTest` with 3 test cases
   - Use `ApiUrlUtils` for endpoints
   - Use `TestDataBuilder` for test input

### **Phase 3: Verification**
7. Run tests locally: `./dev.sh patient-service test`
8. Verify all tests pass against local DB
9. Add to CI workflow if needed

## ✅ **Success Criteria**

- ✅ All 7 integration tests pass against local DB
- ✅ Unique test data prevents conflicts (UUIDs/timestamps)
- ✅ Tests are isolated and can run in any order
- ✅ HIPAA compliant - no patient data deletion
- ✅ Test data remains in DB (as per HIPAA regulations)

## 📝 **Test Data Strategy**

### **Test Data Generation Utilities**
Create utility classes/functions for:
1. **API URL Utilities**: Centralize endpoint URLs (don't hardcode)
   - Use utility class to get each API endpoint URL
   - Example: `ApiUrlUtils.getCreatePatientUrl()`, `ApiUrlUtils.getProfileUrl()`

2. **Test Input Builders**: Helper functions to create test input data
   - Create function to generate `CreatePatientInput` with unique data
   - Use UUIDs/timestamps for uniqueness (email, externalUserId, phone)

3. **Unique Data Generation**
   - Email: `test-{UUID}@example.com` - Guaranteed unique
   - External User ID: `test-external-{timestamp}-{UUID}` - Guaranteed unique
   - Phone: Pattern like `+1{timestamp}` for uniqueness

**Result**: Each test run creates unique data, no conflicts, no cleanup needed

## 🎓 **Testing Best Practices**

1. **Naming**: `test<Action>_<Condition>()` pattern
2. **Isolation**: Each test is independent with unique data
3. **No Cleanup**: HIPAA compliant - patient data is not deleted
4. **Assertions**: Verify both HTTP status and response body
5. **Utility Classes**: Use `ApiUrlUtils` for endpoints, `TestDataBuilder` for test input
6. **Unique Test Data**: Always use UUIDs and timestamps for uniqueness (prevents conflicts)
7. **API-First**: Use actual API endpoints for setup (no SQL scripts)
8. **HIPAA Compliance**: Never delete patient data - test data remains in DB
9. **Code Organization**: Keep test utilities separate from test classes

## 🔄 **Future Enhancements** (Not in this plan)

- Appointment booking integration tests
- Provider service integration tests
- Gateway service integration tests
- Testcontainers for CI environment (optional)

## 📋 **Cleanup Strategy - HIPAA Compliant**

### **Approach: No Cleanup (HIPAA Compliant)**
- ✅ **No DELETE endpoint** - Patient data cannot be deleted per HIPAA
- ✅ **Unique test data** - UUIDs/timestamps prevent conflicts
- ✅ **No cleanup code** - Simpler tests, no `@AfterEach` needed
- ✅ **Data retention** - Test data remains in DB (as required by HIPAA)

### **Benefits**
1. **HIPAA Compliant**: Follows healthcare data retention regulations
2. **Simpler**: No cleanup code to write/maintain
3. **Faster Tests**: No cleanup overhead
4. **Realistic**: Matches production behavior (data is not deleted)

## 📋 **Utility Classes Overview**

### **ApiUrlUtils**
- Purpose: Centralize API endpoint URLs (no hardcoding)
- Location: `services/patient-service/src/test/java/com/healthcare/util/ApiUrlUtils.java`
- Methods: Get URLs for each API endpoint

### **TestDataBuilder**
- Purpose: Helper functions to create test input data
- Location: `services/patient-service/src/test/java/com/healthcare/util/TestDataBuilder.java`
- Methods: Build test input objects with unique data

---

**Status**: 📋 Plan Ready (HIPAA Compliant - No Cleanup, Utility-Based)
**Next Step**: Create utility classes (`ApiUrlUtils`, `TestDataBuilder`), then create `application-test.yml` and first test class
