# Shared Module Implementation Guide

> **🎯 Technical Implementation: Core Entity Structure First**
>
> This document provides detailed technical guidance for implementing the shared module, focusing on getting the core entity structure working before adding authentication and audit features.

## 📋 **Document Information**

- **Document Title**: Shared Module Implementation Guide
- **Version**: 1.0
- **Date**: 2025-01-09
- **Author**: Healthcare AI Team
- **Status**: Active

## 🎯 **Implementation Strategy**

### **Phase 1: Core Entity Structure (Current Focus)**
1. **Complete Entity Cleanup** - Finish all entity implementations
2. **Database Schema** - Deploy and test database structure
3. **Basic Repository Layer** - Implement CRUD operations
4. **Basic Service Layer** - Add business logic

### **Phase 2: Authentication Integration (Next)**
1. **JWT Context Service** - Implement JWT processing
2. **Security Configuration** - Add Spring Security
3. **Audit Listener Enhancement** - Connect to JWT context

## 🏗️ **Phase 1: Core Entity Implementation**

### **1.1 Entity Cleanup Tasks**

#### **Current Status**
- ✅ **BaseEntity** - Complete with audit listener
- ✅ **User** - Complete and clean
- 🔄 **Patient** - Needs cleanup (remove duplicate ID)
- 🔄 **Provider** - Needs cleanup (remove duplicate ID)
- 🔄 **Appointment** - Needs cleanup (remove duplicate ID)
- 🔄 **MedicalRecord** - Needs cleanup (remove duplicate ID)
- 🔄 **AuditLog** - Needs cleanup (remove duplicate ID)

#### **Cleanup Checklist for Each Entity**
- [ ] Remove duplicate `id` field (inherited from BaseEntity)
- [ ] Remove duplicate `getId()` method
- [ ] Remove unnecessary imports (`UUID`, `@Id`, `@GeneratedValue`, `@Column`)
- [ ] Clean up getter/setter organization
- [ ] Add proper validation annotations
- [ ] Test compilation

### **1.2 Database Schema Deployment**

#### **Terraform Deployment Steps**
```bash
# 1. Navigate to infrastructure directory
cd healthcare-infra/terraform

# 2. Initialize Terraform
terraform init

# 3. Plan deployment
terraform plan

# 4. Apply changes
terraform apply
```

#### **Database Verification**
```sql
-- Verify all tables exist
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- Verify all indexes exist
SELECT indexname FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY indexname;

-- Test basic operations
INSERT INTO user_profiles (external_auth_id, first_name, last_name, email, phone, date_of_birth, gender, role)
VALUES ('test-123', 'John', 'Doe', 'john@example.com', '123-456-7890', '1990-01-01', 'MALE', 'PATIENT');
```

### **1.3 Repository Layer Implementation**

#### **BaseRepository Interface**
```java
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, ID extends Serializable> extends JpaRepository<T, ID> {

    // Custom query methods can be added here
    List<T> findByUpdatedBy(String updatedBy);
    List<T> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);
}
```

#### **Entity Repositories**
```java
@Repository
public interface UserRepository extends BaseRepository<User, UUID> {
    Optional<User> findByExternalAuthId(String externalAuthId);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByStatus(UserStatus status);
}

@Repository
public interface PatientRepository extends BaseRepository<Patient, UUID> {
    Optional<Patient> findByUser(User user);
    Optional<Patient> findByPatientNumber(String patientNumber);
    List<Patient> findByUser_Role(UserRole role);
}

@Repository
public interface ProviderRepository extends BaseRepository<Provider, UUID> {
    Optional<Provider> findByUser(User user);
    Optional<Provider> findByNpiNumber(String npiNumber);
    List<Provider> findBySpecialtiesContaining(String specialty);
}

@Repository
public interface AppointmentRepository extends BaseRepository<Appointment, UUID> {
    List<Appointment> findByPatient(Patient patient);
    List<Appointment> findByProvider(Provider provider);
    List<Appointment> findByScheduledAtBetween(OffsetDateTime start, OffsetDateTime end);
    List<Appointment> findByStatus(AppointmentStatus status);
}

@Repository
public interface MedicalRecordRepository extends BaseRepository<MedicalRecord, UUID> {
    List<MedicalRecord> findByAppointment(Appointment appointment);
    List<MedicalRecord> findByIsPatientVisibleTrue();
    List<MedicalRecord> findByRecordType(MedicalRecordType recordType);
}

@Repository
public interface AuditLogRepository extends BaseRepository<AuditLog, UUID> {
    List<AuditLog> findByUser(User user);
    List<AuditLog> findByActionType(ActionType actionType);
    List<AuditLog> findByResourceType(ResourceType resourceType);
    List<AuditLog> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);
}
```

### **1.4 Service Layer Implementation**

#### **BaseService Class**
```java
@Service
public abstract class BaseService<T extends BaseEntity, ID extends Serializable> {

    @Autowired
    protected BaseRepository<T, ID> repository;

    public T findById(ID id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity not found with id: " + id));
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public T save(T entity) {
        return repository.save(entity);
    }

    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    public boolean existsById(ID id) {
        return repository.existsById(id);
    }
}
```

#### **Entity Services**
```java
@Service
public class UserService extends BaseService<User, UUID> {

    @Autowired
    private UserRepository userRepository;

    public User findByExternalAuthId(String externalAuthId) {
        return userRepository.findByExternalAuthId(externalAuthId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with external auth id: " + externalAuthId));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public User createUser(UserCreateRequest request) {
        User user = new User(
            request.getExternalAuthId(),
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getPhone(),
            request.getDateOfBirth(),
            request.getGender(),
            request.getRole()
        );
        return save(user);
    }
}
```

## 🔐 **Phase 2: Authentication Integration**

### **2.1 JWT Context Service Implementation**

#### **JwtContextService Implementation**
```java
@Service
public class JwtContextServiceImpl implements JwtContextService {

    @Override
    public String getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) auth.getPrincipal();
                return jwtToken.getToken().getClaimAsString("sub");
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to extract user ID from JWT context", e);
            return null;
        }
    }

    @Override
    public String getCurrentExternalAuthId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) auth.getPrincipal();
                return jwtToken.getToken().getClaimAsString("external_auth_id");
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to extract external auth ID from JWT context", e);
            return null;
        }
    }

    @Override
    public boolean hasRole(String role) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
        } catch (Exception e) {
            log.warn("Failed to check role from JWT context", e);
            return false;
        }
    }

    @Override
    public boolean isAuthenticated() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.isAuthenticated();
        } catch (Exception e) {
            log.warn("Failed to check authentication status", e);
            return false;
        }
    }
}
```

### **2.2 Audit Listener Enhancement**

#### **Update BaseEntity AuditListener**
```java
public static class AuditListener {

    @Autowired
    private static JwtContextService jwtContextService;

    @PrePersist
    public void prePersist(BaseEntity entity) {
        String currentUserId = getCurrentUserIdFromJWT();
        entity.setUpdatedBy(currentUserId);
    }

    @PreUpdate
    public void preUpdate(BaseEntity entity) {
        String currentUserId = getCurrentUserIdFromJWT();
        entity.setUpdatedBy(currentUserId);
    }

    private String getCurrentUserIdFromJWT() {
        try {
            if (jwtContextService != null) {
                String userId = jwtContextService.getCurrentUserId();
                if (userId != null) {
                    return userId;
                }
            }
            return "system";
        } catch (Exception e) {
            log.warn("Failed to extract user ID from JWT context", e);
            return "system";
        }
    }
}
```

## 🧪 **Testing Strategy**

### **Unit Tests**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setExternalAuthId("auth-123");
        request.setFirstName("John");
        // ... set other fields

        // When
        User result = userService.createUser(request);

        // Then
        assertThat(result.getExternalAuthId()).isEqualTo("auth-123");
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(userRepository).save(any(User.class));
    }
}
```

### **Integration Tests**
```java
@SpringBootTest
@Transactional
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByExternalAuthId() {
        // Given
        User user = new User("auth-123", "John", "Doe", "john@example.com",
                           "123-456-7890", LocalDate.of(1990, 1, 1), Gender.MALE, UserRole.PATIENT);
        userRepository.save(user);

        // When
        Optional<User> result = userRepository.findByExternalAuthId("auth-123");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("John");
    }
}
```

## 📋 **Implementation Checklist**

### **Phase 1: Core Entity Structure**
- [ ] Clean up all entities (remove duplicate ID fields)
- [ ] Deploy database schema via Terraform
- [ ] Implement BaseRepository interface
- [ ] Implement entity repositories
- [ ] Implement BaseService class
- [ ] Implement entity services
- [ ] Write unit tests for repositories
- [ ] Write unit tests for services
- [ ] Write integration tests
- [ ] Verify all tests pass

### **Phase 2: Authentication Integration**
- [ ] Implement JwtContextService
- [ ] Update AuditListener to use JWT context
- [ ] Add Spring Security configuration
- [ ] Test JWT authentication flow
- [ ] Test automatic audit population
- [ ] Write authentication tests
- [ ] Verify audit trail is working

## 🚀 **Next Steps**

1. **Start with Entity Cleanup** - Complete all entity implementations
2. **Deploy Database Schema** - Get database working
3. **Implement Repository Layer** - Add CRUD operations
4. **Implement Service Layer** - Add business logic
5. **Add Authentication** - Implement JWT processing
6. **Enable Audit Trail** - Connect audit to JWT context

---

**Remember**: Focus on getting the core working first. Each phase should deliver working functionality that can be tested and validated.
