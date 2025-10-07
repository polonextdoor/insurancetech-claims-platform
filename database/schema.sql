-- InsuranceTech Claims Platform Database Schema
-- PostgreSQL 15

-- Drop existing tables (for development resets)
DROP TABLE IF EXISTS claim_documents CASCADE;
DROP TABLE IF EXISTS fraud_alerts CASCADE;
DROP TABLE IF EXISTS claim_events CASCADE;
DROP TABLE IF EXISTS claims CASCADE;
DROP TABLE IF EXISTS policies CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS claim_status CASCADE;
DROP TYPE IF EXISTS policy_type CASCADE;
DROP TYPE IF EXISTS risk_level CASCADE;

-- Custom ENUM types
CREATE TYPE user_role AS ENUM ('CUSTOMER', 'AGENT', 'ADJUSTER', 'ADMIN');
CREATE TYPE claim_status AS ENUM ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'INVESTIGATING', 'APPROVED', 'DENIED', 'CLOSED');
CREATE TYPE policy_type AS ENUM ('AUTO', 'HOME', 'HEALTH', 'LIFE', 'BUSINESS');
CREATE TYPE risk_level AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');

-- Users table (customers, agents, adjusters, admins)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role user_role NOT NULL DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Policies table
CREATE TABLE policies (
    id BIGSERIAL PRIMARY KEY,
    policy_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    policy_type policy_type NOT NULL,
    coverage_amount DECIMAL(12, 2) NOT NULL,
    deductible DECIMAL(10, 2) NOT NULL,
    premium_amount DECIMAL(10, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_dates CHECK (end_date > start_date),
    CONSTRAINT positive_amounts CHECK (
        coverage_amount > 0 AND 
        deductible >= 0 AND 
        premium_amount > 0
    )
);

-- Claims table
CREATE TABLE claims (
    id BIGSERIAL PRIMARY KEY,
    claim_number VARCHAR(50) UNIQUE NOT NULL,
    policy_id BIGINT NOT NULL REFERENCES policies(id) ON DELETE RESTRICT,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_adjuster_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    
    -- Claim details
    incident_date DATE NOT NULL,
    reported_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    incident_description TEXT NOT NULL,
    incident_location VARCHAR(255),
    
    -- Financial details
    claimed_amount DECIMAL(12, 2) NOT NULL,
    approved_amount DECIMAL(12, 2),
    deductible_amount DECIMAL(10, 2),
    
    -- Status and workflow
    status claim_status NOT NULL DEFAULT 'DRAFT',
    
    -- Risk assessment
    risk_score INTEGER DEFAULT 0,
    risk_level risk_level DEFAULT 'LOW',
    
    -- Fraud detection
    fraud_flag BOOLEAN DEFAULT false,
    fraud_score DECIMAL(5, 2) DEFAULT 0.0,
    
    -- Timestamps
    submitted_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    closed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT positive_claimed_amount CHECK (claimed_amount > 0),
    CONSTRAINT valid_incident_date CHECK (incident_date <= CURRENT_DATE),
    CONSTRAINT valid_fraud_score CHECK (fraud_score >= 0 AND fraud_score <= 100)
);

-- Claim events (audit trail / status history)
CREATE TABLE claim_events (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    old_status claim_status,
    new_status claim_status,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Claim documents (photos, PDFs, etc.)
CREATE TABLE claim_documents (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    uploaded_by BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL, -- 'PHOTO', 'POLICE_REPORT', 'ESTIMATE', 'RECEIPT'
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    s3_bucket VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT positive_file_size CHECK (file_size > 0)
);

-- Fraud alerts
CREATE TABLE fraud_alerts (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    alert_type VARCHAR(100) NOT NULL, -- 'MULTIPLE_CLAIMS', 'HIGH_AMOUNT', 'PATTERN_MATCH'
    severity risk_level NOT NULL,
    description TEXT NOT NULL,
    is_resolved BOOLEAN DEFAULT false,
    resolved_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_policies_user_id ON policies(user_id);
CREATE INDEX idx_policies_policy_number ON policies(policy_number);
CREATE INDEX idx_policies_active ON policies(is_active, end_date);
CREATE INDEX idx_claims_user_id ON claims(user_id);
CREATE INDEX idx_claims_policy_id ON claims(policy_id);
CREATE INDEX idx_claims_status ON claims(status);
CREATE INDEX idx_claims_adjuster ON claims(assigned_adjuster_id);
CREATE INDEX idx_claims_fraud_flag ON claims(fraud_flag);
CREATE INDEX idx_claims_incident_date ON claims(incident_date);
CREATE INDEX idx_claim_events_claim_id ON claim_events(claim_id);
CREATE INDEX idx_claim_events_created_at ON claim_events(created_at);
CREATE INDEX idx_claim_documents_claim_id ON claim_documents(claim_id);
CREATE INDEX idx_fraud_alerts_claim_id ON fraud_alerts(claim_id);
CREATE INDEX idx_fraud_alerts_resolved ON fraud_alerts(is_resolved);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for automatic updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_policies_updated_at BEFORE UPDATE ON policies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_claims_updated_at BEFORE UPDATE ON claims
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data for development
INSERT INTO users (email, password_hash, first_name, last_name, phone, role) VALUES
('admin@insurancetech.com', '$2a$10$dummyhash1', 'Admin', 'User', '555-0001', 'ADMIN'),
('adjuster@insurancetech.com', '$2a$10$dummyhash2', 'Jane', 'Adjuster', '555-0002', 'ADJUSTER'),
('customer@example.com', '$2a$10$dummyhash3', 'John', 'Customer', '555-0003', 'CUSTOMER');

INSERT INTO policies (policy_number, user_id, policy_type, coverage_amount, deductible, premium_amount, start_date, end_date) VALUES
('POL-AUTO-001', 3, 'AUTO', 50000.00, 500.00, 1200.00, '2024-01-01', '2025-01-01'),
('POL-HOME-001', 3, 'HOME', 250000.00, 1000.00, 1800.00, '2024-01-01', '2025-01-01');

-- Views for common queries
CREATE VIEW active_claims_summary AS
SELECT 
    c.id,
    c.claim_number,
    c.status,
    c.claimed_amount,
    c.risk_level,
    c.fraud_flag,
    u.first_name || ' ' || u.last_name AS customer_name,
    p.policy_number,
    p.policy_type,
    c.incident_date,
    c.created_at
FROM claims c
JOIN users u ON c.user_id = u.id
JOIN policies p ON c.policy_id = p.id
WHERE c.status NOT IN ('CLOSED', 'DENIED')
ORDER BY c.created_at DESC;

CREATE VIEW fraud_dashboard AS
SELECT 
    c.id AS claim_id,
    c.claim_number,
    c.fraud_score,
    c.risk_level,
    COUNT(fa.id) AS alert_count,
    u.first_name || ' ' || u.last_name AS customer_name,
    c.claimed_amount,
    c.status
FROM claims c
JOIN users u ON c.user_id = u.id
LEFT JOIN fraud_alerts fa ON c.id = fa.claim_id AND fa.is_resolved = false
WHERE c.fraud_flag = true
GROUP BY c.id, c.claim_number, c.fraud_score, c.risk_level, u.first_name, u.last_name, c.claimed_amount, c.status
ORDER BY c.fraud_score DESC;

-- Grant permissions (adjust for your needs)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO insurancetech_app;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO insurancetech_app;