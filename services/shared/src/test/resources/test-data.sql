-- Test data for PostgreSQL test database
-- This ensures enum values work correctly in tests

-- Insert test users
INSERT INTO user_profiles (id, external_auth_id, first_name, last_name, email, phone, date_of_birth, gender, role, status) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'test_auth_001', 'John', 'Doe', 'john.doe@test.com', '+1234567890', '1990-01-01', 'MALE', 'PATIENT', 'ACTIVE'),
('550e8400-e29b-41d4-a716-446655440001', 'test_auth_002', 'Jane', 'Smith', 'jane.smith@test.com', '+1234567891', '1985-05-15', 'FEMALE', 'PROVIDER', 'ACTIVE'),
('550e8400-e29b-41d4-a716-446655440002', 'test_auth_003', 'Bob', 'Johnson', 'bob.johnson@test.com', '+1234567892', '1975-12-10', 'MALE', 'PATIENT', 'INACTIVE');

-- Insert test patients
INSERT INTO patient_profiles (id, user_id, patient_number, medical_history, allergies, emergency_contact_name, emergency_contact_phone, emergency_contact_relationship, insurance_provider, insurance_policy_number) VALUES
('650e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440000', 'PAT-001', '{"conditions": ["Hypertension"], "medications": ["Lisinopril"]}', '{"allergies": ["Penicillin"]}', 'Emergency Contact', '+1987654321', 'Spouse', 'Test Insurance', 'POL-123456789');

-- Insert test providers
INSERT INTO provider_profiles (id, user_id, npi_number, specialties, qualifications, bio, office_address, office_phone) VALUES
('750e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440001', '1234567890', '{"Cardiology", "Internal Medicine"}', 'MD, Board Certified in Cardiology', 'Experienced cardiologist with 15 years of practice', '123 Medical Center Dr', '+1555123456');

-- Insert test appointments
INSERT INTO appointments (id, patient_id, provider_id, scheduled_at, appointment_type, status, notes) VALUES
('850e8400-e29b-41d4-a716-446655440000', '650e8400-e29b-41d4-a716-446655440000', '750e8400-e29b-41d4-a716-446655440000', '2025-02-01 10:00:00+00', 'REGULAR_CONSULTATION', 'SCHEDULED', 'Follow-up appointment for hypertension management');

-- Insert test medical records
INSERT INTO medical_records (id, appointment_id, record_type, content, is_patient_visible, release_date) VALUES
('950e8400-e29b-41d4-a716-446655440000', '850e8400-e29b-41d4-a716-446655440000', 'DIAGNOSIS', 'Patient presents with well-controlled hypertension. Blood pressure readings within normal range. Continue current medication regimen.', true, '2025-02-01 12:00:00+00');

-- Insert test audit logs
INSERT INTO audit_logs (id, action, resource_type, resource_id, user_id, outcome, details, source_ip, user_agent) VALUES
('a50e8400-e29b-41d4-a716-446655440000', 'CREATE', 'USER_PROFILE', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440001', 'SUCCESS', '{"field": "email", "old_value": null, "new_value": "john.doe@test.com"}', '192.168.1.100', 'Mozilla/5.0 (Test Browser)');
