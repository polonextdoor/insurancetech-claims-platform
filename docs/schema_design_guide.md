# Database Schema Design Guide - Deep Dive
## InsuranceTech Claims Platform

This guide explains every design decision in the database schema, helping you understand WHY each choice was made and HOW to think through database design for future projects.

---

## Table of Contents
1. [Core Design Principles](#core-design-principles)
2. [Entity Relationship Breakdown](#entity-relationship-breakdown)
3. [Data Type Choices](#data-type-choices)
4. [Constraint Strategy](#constraint-strategy)
5. [Index Strategy](#index-strategy)
6. [Performance Optimizations](#performance-optimizations)
7. [Interview Talking Points](#interview-talking-points)

---

## Core Design Principles

### 1. Normalization (Avoiding Data Duplication)
**What it means**: Each piece of data exists in only ONE place.

**Example in our schema**:
```sql
-- BAD (denormalized):
claims table with: user_email, user_name, user_phone, policy_number, policy_type...
-- Problem: If user changes email, you update 100 places

-- GOOD (normalized):
claims → references user_id → users table has email once
claims → references policy_id → policies table has policy info once
```

**Why this matters**:
- Data consistency (change once, affects everywhere)
- Less storage space
- Easier to maintain
- Prevents "update anomalies" (forgetting to update all copies)

### 2. Foreign Keys (Relationships Between Tables)
**What they do**: Enforce data integrity by ensuring references exist.

**In our schema**:
```sql
policy_id BIGINT NOT NULL REFERENCES policies(id) ON DELETE RESTRICT
```

**Breaking this down**:
- `REFERENCES policies(id)`: Must be a valid policy ID
- `ON DELETE RESTRICT`: Can't delete a policy if claims exist (protects data)
- `NOT NULL`: Every claim MUST have a policy (business rule)

**Different DELETE behaviors**:
- `RESTRICT`: Block deletion (used for policies - can't delete if claims exist)
- `CASCADE`: Delete related records too (used for claim_documents - if claim deleted, delete docs)
- `SET NULL`: Set to null (used for assigned_adjuster - if adjuster deleted, claim remains)

### 3. Data Integrity Through Constraints
**Purpose**: Database enforces business rules automatically.

**Examples from our schema**:
```sql
CONSTRAINT valid_dates CHECK (end_date > start_date)
-- Prevents impossible scenarios like policy ending before it starts

CONSTRAINT positive_claimed_amount CHECK (claimed_amount > 0)
-- Can't file a claim for $0 or negative amount

CONSTRAINT valid_fraud_score CHECK (fraud_score >= 0 AND fraud_score <= 100)
-- Fraud scores are percentages, must be 0-100
```

**Why in database vs application code**:
- Last line of defense (even if app has bugs)
- Works for ALL applications accessing database
- Enforced at data layer (can't be bypassed)
- Clear documentation of business rules

---

## Entity Relationship Breakdown

### Table Creation Order (Why This Matters)

**Rule**: Create parent tables before child tables (tables that are referenced must exist first).

**Our order**:
1. **ENUM types** → Used by multiple tables
2. **users** → Referenced by policies, claims, events
3. **policies** → Referenced by claims
4. **claims** → Referenced by claim_events, claim_documents, fraud_alerts
5. **Child tables** → claim_events, claim_documents, fraud_alerts

**Why this order**:
```sql
-- This FAILS if users table doesn't exist yet:
CREATE TABLE policies (
    user_id BIGINT REFERENCES users(id)  -- ERROR: users doesn't exist!
);
```

### 1. Users Table - The Foundation

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'CUSTOMER',
    ...
);
```

**Design Decisions**:

**BIGSERIAL for ID**:
- `SERIAL` = auto-incrementing integer (max ~2 billion)
- `BIGSERIAL` = auto-incrementing big integer (max ~9 quintillion)
- **Why BIGSERIAL?** Future-proofing. Never run out of IDs.

**VARCHAR(255) for email**:
- Email max length is technically 320, but 255 is standard
- **Why 255?** Old MySQL optimization (255 was max for indexing)
- Modern practice: Use VARCHAR without length, but 255 is safe

**email UNIQUE constraint**:
- Each email can only exist once
- Prevents duplicate accounts
- Automatically creates an index (fast lookups)

**password_hash NOT password**:
- **NEVER** store plain passwords
- Store bcrypt/argon2 hash
- Even if database breached, passwords are safe

**ENUM for role**:
```sql
CREATE TYPE user_role AS ENUM ('CUSTOMER', 'AGENT', 'ADJUSTER', 'ADMIN');
```
- **Why ENUM vs VARCHAR?** Database enforces valid values
- Can't accidentally insert role='MANAGER' (not in enum)
- Memory efficient (stored as integers internally)
- Self-documenting (schema shows all valid roles)

**Timestamps (created_at, updated_at)**:
```sql
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
```
- **Audit trail**: When was record created/modified?
- **Debugging**: Track data changes
- **Compliance**: Many regulations require this
- **updated_at trigger**: Automatically updates on any change

### 2. Policies Table - Customer Insurance Policies

```sql
CREATE TABLE policies (
    id BIGSERIAL PRIMARY KEY,
    policy_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    coverage_amount DECIMAL(12, 2) NOT NULL,
    ...
);
```

**Design Decisions**:

**policy_number VARCHAR(50) UNIQUE**:
- Business identifier (not database ID)
- Format: "POL-AUTO-001" (human-readable)
- UNIQUE ensures no duplicate policy numbers
- **Why separate from ID?** ID is internal, policy_number is customer-facing

**DECIMAL(12, 2) for money**:
- `DECIMAL(12, 2)` = 12 total digits, 2 after decimal
- Max value: 9,999,999,999.99 (10 billion)
- **Why DECIMAL not FLOAT?** 
  - FLOAT has rounding errors: 0.1 + 0.2 ≠ 0.3
  - DECIMAL is exact (critical for money!)
  - Example: $0.10 + $0.20 = $0.30 (always)

**user_id with CASCADE delete**:
```sql
user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
```
- **Why CASCADE here?** If user deleted, delete their policies too
- Business decision: User owns policies
- Alternative: `ON DELETE RESTRICT` prevents user deletion if policies exist

**Date validation**:
```sql
CONSTRAINT valid_dates CHECK (end_date > start_date)
```
- Impossible to create policy ending before it starts
- Catches data entry errors at database level

### 3. Claims Table - The Core Entity

```sql
CREATE TABLE claims (
    id BIGSERIAL PRIMARY KEY,
    claim_number VARCHAR(50) UNIQUE NOT NULL,
    policy_id BIGINT NOT NULL REFERENCES policies(id) ON DELETE RESTRICT,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_adjuster_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    ...
);
```

**Design Decisions**:

**Multiple foreign keys to users table**:
```sql
user_id BIGINT NOT NULL REFERENCES users(id)           -- Who filed claim
assigned_adjuster_id BIGINT REFERENCES users(id)       -- Who's reviewing it
```
- **Same table, different meanings** (self-referential)
- user_id: claim owner (customer)
- assigned_adjuster_id: claim handler (employee)
- **Why separate?** Track both relationships independently

**Different DELETE behaviors**:
```sql
policy_id → ON DELETE RESTRICT   -- Can't delete policy with active claims
user_id → ON DELETE CASCADE       -- If customer deleted, delete their claims
assigned_adjuster_id → ON DELETE SET NULL  -- If adjuster deleted, claim remains
```
- **Why different?** Business rules vary:
  - Policies: Protect financial data
  - Customers: Remove all traces (privacy)
  - Adjusters: Employee leaves, reassign claims

**claimed_amount vs approved_amount**:
```sql
claimed_amount DECIMAL(12, 2) NOT NULL,      -- What customer asks for
approved_amount DECIMAL(12, 2),              -- What insurance pays (nullable)
```
- **Why both?** They're different values
- claimed_amount: Always set (required)
- approved_amount: NULL until claim approved
- **Reporting**: Track difference (fraud indicator)

**Status field with ENUM**:
```sql
status claim_status NOT NULL DEFAULT 'DRAFT',

CREATE TYPE claim_status AS ENUM (
    'DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 
    'INVESTIGATING', 'APPROVED', 'DENIED', 'CLOSED'
);
```
- **Workflow states**: Shows claim lifecycle
- **Why ENUM?** Prevents invalid states ('PENDNG' typo)
- **DEFAULT 'DRAFT'**: New claims start here
- **Order matters**: Shows progression (draft → submitted → review → closed)

**Risk and Fraud fields**:
```sql
risk_score INTEGER DEFAULT 0,
risk_level risk_level DEFAULT 'LOW',
fraud_flag BOOLEAN DEFAULT false,
fraud_score DECIMAL(5, 2) DEFAULT 0.0,
```
- **Computed values**: Calculated by application
- **Denormalized**: Could recalculate, but stored for speed
- **Why store?** 
  - Avoid recalculating every query
  - Historical tracking (score when approved, not now)
  - Faster reporting/analytics

### 4. Claim Events Table - Audit Trail

```sql
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
```

**Purpose**: Track every change to a claim.

**Why needed**:
- **Compliance**: Regulations require audit trails
- **Debugging**: "Who changed this and when?"
- **Customer service**: Show claim history
- **Fraud detection**: Unusual patterns in changes

**Design choices**:

**old_status and new_status both stored**:
- Shows state transition: SUBMITTED → UNDER_REVIEW
- Better than just new_status (lose context)
- Easy to query: "Show all claims that went from SUBMITTED to DENIED"

**TEXT for notes (not VARCHAR)**:
- TEXT = unlimited length
- VARCHAR(n) = limited to n characters
- Notes could be long (adjuster comments)
- **No performance penalty** in modern PostgreSQL

**ON DELETE CASCADE**:
- If claim deleted, delete its history too
- If user deleted, delete their actions (privacy)
- Alternative: Keep events but anonymize user

### 5. Claim Documents Table - File Attachments

```sql
CREATE TABLE claim_documents (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    s3_key VARCHAR(500) NOT NULL,
    s3_bucket VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    ...
);
```

**Design Decisions**:

**Store S3 path, not file content**:
```sql
s3_key VARCHAR(500) NOT NULL,      -- Path in S3
s3_bucket VARCHAR(255) NOT NULL,   -- Which bucket
-- NOT: file_content BYTEA          -- Don't store in database!
```
- **Why not in database?** 
  - Files can be huge (photos, PDFs)
  - Database gets bloated
  - Slow queries
  - Expensive backups
- **Store reference**: Database has pointer, S3 has file

**file_size as BIGINT**:
- Tracks file size in bytes
- **Why BIGINT?** 2GB+ files possible
- Used for: Storage quotas, billing, validation

**document_type categorization**:
```sql
document_type VARCHAR(50) NOT NULL, -- 'PHOTO', 'POLICE_REPORT', 'ESTIMATE'
```
- **Why not ENUM?** More flexible than ENUM
- Can add types without altering schema
- Business logic in application
- **Trade-off**: No database validation

### 6. Fraud Alerts Table - Detection System

```sql
CREATE TABLE fraud_alerts (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    alert_type VARCHAR(100) NOT NULL,
    severity risk_level NOT NULL,
    is_resolved BOOLEAN DEFAULT false,
    resolved_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    ...
);
```

**Purpose**: Track suspicious patterns requiring investigation.

**Why separate table**:
- One claim can have multiple alerts
- Alerts have lifecycle (created → investigated → resolved)
- Historical tracking (even after resolved)

**resolved_by with SET NULL**:
```sql
resolved_by BIGINT REFERENCES users(id) ON DELETE SET NULL
```
- **Why SET NULL?** Keep alert even if investigator leaves
- Alternative: RESTRICT (can't delete user with open alerts)
- Business decision: Alert data more important than user reference

---

## Data Type Choices - When to Use What

### Integer Types
```sql
SERIAL      -- Auto-increment, up to 2.1 billion
BIGSERIAL   -- Auto-increment, up to 9.2 quintillion
INTEGER     -- Manual numbers, -2B to +2B
BIGINT      -- Manual numbers, -9.2Q to +9.2Q
```

**Our choices**:
- IDs: **BIGSERIAL** (never run out)
- Foreign keys: **BIGINT** (match ID type)
- Scores: **INTEGER** (0-100 range, no decimals needed)

### Decimal vs Numeric vs Float

```sql
DECIMAL(12, 2)  -- Exact precision (money)
NUMERIC(12, 2)  -- Same as DECIMAL
FLOAT           -- Approximate (scientific calculations)
```

**Our choice**: DECIMAL for all money
- Why? Exactness required for financial data
- Example: 3 payments of $0.33 = $0.99, not $1.00

### String Types

```sql
VARCHAR(n)   -- Variable length, max n characters
TEXT         -- Unlimited length
CHAR(n)      -- Fixed length (padded)
```

**Our choices**:
- Emails, names: **VARCHAR(255)** (reasonable limit)
- Notes, descriptions: **TEXT** (unknown length)
- Never CHAR: Wasteful for variable data

### Date/Time Types

```sql
DATE           -- Just date: 2024-01-01
TIME           -- Just time: 14:30:00
TIMESTAMP      -- Date + time: 2024-01-01 14:30:00
TIMESTAMPTZ    -- Timestamp with timezone
```

**Our choices**:
- incident_date: **DATE** (don't need exact time)
- created_at: **TIMESTAMP** (track exact moment)
- Why not TIMESTAMPTZ? Depends on app architecture

### Boolean

```sql
BOOLEAN  -- true/false/null
```

**Our uses**:
- is_active, fraud_flag, is_resolved
- Clear binary states
- Indexable for filtering

---

## Constraint Strategy

### Primary Keys

```sql
id BIGSERIAL PRIMARY KEY
```

**What it guarantees**:
- UNIQUE: No duplicate IDs
- NOT NULL: Every row has ID
- INDEXED: Fast lookups by ID

**Why BIGSERIAL**:
- Auto-generates values (no conflicts)
- Surrogate key (technical, not business meaning)
- Simple: Just a number

### Unique Constraints

```sql
email VARCHAR(255) UNIQUE
policy_number VARCHAR(50) UNIQUE
claim_number VARCHAR(50) UNIQUE
```

**Why needed**:
- Business requirement (can't have 2 same policy numbers)
- Automatically creates index (fast lookups)
- Prevents application bugs

### Foreign Keys

```sql
user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
```

**What it guarantees**:
- Value exists in parent table
- Can't insert orphaned records
- Enforces relationships

**Performance note**: Foreign keys add overhead on INSERT/DELETE but guarantee consistency

### Check Constraints

```sql
CONSTRAINT positive_amounts CHECK (
    coverage_amount > 0 AND 
    deductible >= 0 AND 
    premium_amount > 0
)
```

**Why use them**:
- Business logic in database
- Can't be bypassed
- Self-documenting
- Catches bugs

**When not to use**:
- Complex logic (better in application)
- Frequent changes (requires migration)
- Cross-table validation (use triggers)

### NOT NULL Constraints

```sql
email VARCHAR(255) NOT NULL
```

**Decision process**:
- Required field? → NOT NULL
- Optional field? → Allow NULL
- Default value? → NOT NULL with DEFAULT

**Our patterns**:
- All IDs: NOT NULL
- User info: email NOT NULL, phone nullable
- Money: claimed_amount NOT NULL, approved_amount nullable

---

## Index Strategy

### What Indexes Do

**Without index**:
```sql
SELECT * FROM claims WHERE user_id = 123;
-- Scans ALL rows (slow for millions of records)
```

**With index**:
```sql
CREATE INDEX idx_claims_user_id ON claims(user_id);
-- Direct lookup (fast, like a book index)
```

### Our Index Decisions

```sql
-- Foreign keys that are frequently queried
CREATE INDEX idx_claims_user_id ON claims(user_id);
CREATE INDEX idx_claims_policy_id ON claims(policy_id);

-- Status for dashboard filtering
CREATE INDEX idx_claims_status ON claims(status);

-- Date range queries (analytics)
CREATE INDEX idx_claims_incident_date ON claims(incident_date);

-- Fraud detection queries
CREATE INDEX idx_claims_fraud_flag ON claims(fraud_flag);
```

**Why these specific indexes**:

1. **Foreign keys**: JOIN operations use them constantly
2. **Status**: Dashboard shows "active claims" (WHERE status = 'UNDER_REVIEW')
3. **Dates**: Reports like "claims this month"
4. **Boolean flags**: Filter active/inactive, fraud/clean

**Indexes we DIDN'T create**:
- Primary keys (automatic)
- UNIQUE fields (automatic)
- TEXT fields (description, notes) - full-text search better
- Low-cardinality (gender with 2 values) - not selective enough

### Composite Indexes

```sql
-- If you often query: status AND assigned_adjuster together
CREATE INDEX idx_claims_status_adjuster ON claims(status, assigned_adjuster_id);
```

**Our decision**: Start simple, add composite indexes if needed
- Over-indexing slows INSERT/UPDATE
- Monitor query performance first
- Add composite when query patterns are clear

---

## Performance Optimizations

### 1. Timestamp Triggers (Auto-update updated_at)

```sql
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

**Why this approach**:
- Automatic: No application code needed
- Consistent: All updates tracked
- Accurate: Database time, not app server time

**Alternative**: Update in application (less reliable)

### 2. Views for Complex Queries

```sql
CREATE VIEW active_claims_summary AS
SELECT 
    c.id,
    c.claim_number,
    u.first_name || ' ' || u.last_name AS customer_name,
    p.policy_number,
    c.claimed_amount,
    c.status
FROM claims c
JOIN users u ON c.user_id = u.id
JOIN policies p ON c.policy_id = p.id
WHERE c.status NOT IN ('CLOSED', 'DENIED');
```

**Benefits**:
- Simplifies application queries
- Consistent JOINs across app
- Easier to optimize (materialized views)
- Abstraction layer

**Usage**:
```sql
-- Instead of complex JOIN in every query
SELECT * FROM active_claims_summary WHERE customer_name LIKE 'John%';
```

### 3. Connection Pooling Configuration

```yaml
hikari:
  maximum-pool-size: 10
  minimum-idle: 5
```

**Why it matters**:
- Database connections are expensive
- Pool reuses connections
- Faster response times
- Handles traffic spikes

**Our settings**:
- 10 max: Enough for development
- 5 minimum: Always ready
- Production: Increase based on load

---

## Interview Talking Points

### Question: "Walk me through your database design process"

**Answer structure**:
1. **Identify entities**: Users, Policies, Claims, Documents, Alerts
2. **Define relationships**: One user has many policies, one policy has many claims
3. **Normalize**: Break into separate tables to avoid duplication
4. **Add constraints**: Enforce business rules (amounts > 0, valid dates)
5. **Index strategically**: Foreign keys, frequently queried fields
6. **Plan for growth**: BIGSERIAL, TEXT instead of VARCHAR, audit trails

### Question: "Why did you choose PostgreSQL over MySQL?"

**Our reasons**:
1. **Better JSON support**: Useful for flexible data
2. **Advanced features**: CTEs, window functions, better ENUM handling
3. **Standards compliant**: True ACID compliance
4. **Enterprise adoption**: Common in insurance/finance
5. **PostGIS extensions**: If location features added

### Question: "How would you handle 10 million claims?"

**Scaling strategy**:
1. **Partitioning**: Partition claims by date (yearly tables)
2. **Archiving**: Move closed claims to separate schema
3. **Read replicas**: Separate analytics queries
4. **Caching**: Redis for hot data
5. **Indexes**: Critical for large datasets
6. **Query optimization**: Avoid N+1, use EXPLAIN

### Question: "How do you ensure data integrity?"

**Our approach**:
1. **Foreign keys**: Referential integrity enforced
2. **Check constraints**: Business rule validation
3. **NOT NULL**: Required fields enforced
4. **UNIQUE constraints**: No duplicate business keys
5. **Transactions**: Atomic operations
6. **Audit trails**: Track all changes

### Question: "What's your strategy for handling money in databases?"

**Answer**:
1. **DECIMAL never FLOAT**: Exact precision required
2. **Store in smallest unit**: Cents not dollars (avoids rounding)
3. **Two decimal places**: DECIMAL(12, 2) standard
4. **Check constraints**: Positive amounts only
5. **Audit all changes**: Financial compliance

---

## Design Patterns Used

### 1. Audit Trail Pattern
- claim_events table tracks all changes
- Who, what, when for compliance
- Historical reporting capability

### 2. Soft Delete Pattern
- is_active boolean instead of DELETE
- Preserves historical data
- Can "undelete" records

### 3. Status Machine Pattern
- ENUM defines valid states
- claim_events tracks transitions
- Prevents invalid state changes

### 4. Reference Data Pattern
- Document pointers (s3_key) not content
- Keeps database lean
- Separates concerns

### 5. Denormalization for Performance
- risk_score stored (could recalculate)
- Trade-off: Storage vs compute
- Faster queries, consistent snapshots

---

## Common Mistakes We Avoided

### ❌ Don't Do This:

```sql
-- Storing JSON blobs for structured data
CREATE TABLE claims (
    data JSON  -- Bad: Can't query efficiently, no constraints
);

-- Using VARCHAR for money
premium_amount VARCHAR(20)  -- Bad: Can't do math, sorting wrong

-- No foreign keys
policy_id INTEGER  -- Bad: Orphaned records possible

-- Storing files in database
photo BYTEA  -- Bad: Database bloat, slow queries

-- Generic "type" field with magic numbers
type INTEGER  -- Bad: What does 3 mean?
```

### ✅ We Did This Instead:

```sql
-- Proper columns with types
CREATE TABLE claims (
    claimed_amount DECIMAL(12, 2),
    policy_id BIGINT REFERENCES policies(id),
    status claim_status,
    s3_key VARCHAR(500)  -- Reference to file
);
```

---

## Future Enhancements

If you want to add complexity later:

### 1. Multi-tenancy
```sql
ALTER TABLE users ADD COLUMN tenant_id BIGINT;
-- Separate data by insurance company
```

### 2. Soft Deletes
```sql
ALTER TABLE claims ADD COLUMN deleted_at TIMESTAMP;
-- Keep data, mark as deleted
```

### 3. Full-Text Search
```sql
ALTER TABLE claims ADD COLUMN search_vector tsvector;
CREATE INDEX idx_search ON claims USING gin(search_vector);
-- Fast text searching
```

### 4. Partitioning
```sql
CREATE TABLE claims_2024 PARTITION OF claims
FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');
-- Split large tables by date
```

### 5. Geospatial Data
```sql
ALTER TABLE claims ADD COLUMN incident_location GEOGRAPHY(POINT);
-- Store lat/long, query by distance
```

---

## Key Takeaways

**Database design is about**:
1. **Clarity**: Anyone should understand the structure
2. **Integrity**: Impossible to create invalid data
3. **Performance**: Fast queries through good indexes
4. **Maintainability**: Easy to modify as requirements change
5. **Security**: Protect sensitive data (passwords hashed, audit trails)

**The thought process**:
1. Model real-world entities
2. Define relationships clearly
3. Use appropriate data types
4. Add constraints for business rules
5. Index for common queries
6. Document your decisions

**Remember**: Perfect is the enemy of good. Start simple, iterate based on actual usage patterns.

---

## Practice Exercise

Try designing a schema for a different domain:
- **E-commerce**: Products, Orders, Customers, Inventory
- **Hospital**: Patients, Appointments, Doctors, Medical Records
- **School**: Students, Courses, Enrollments, Grades

Ask yourself:
- What are the entities?
- How do they relate?
- What constraints are needed?
- What will be queried frequently?
- How will it scale?

This schema took years of experience to design. Understanding WHY each choice was made is more valuable than memorizing the structure.