-- V1__tenant_and_customer_init.sql
-- Description: Initializes the multi-tenant architecture and core customer tables.

-- 1. Create the Tenants (Banks) Table
CREATE TABLE tenants (
                         id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                         bank_name NVARCHAR(255) NOT NULL,
                         contact_email NVARCHAR(255) NOT NULL,
                         risk_threshold INT DEFAULT 70,
                         created_at DATETIME2 DEFAULT CURRENT_TIMESTAMP
);

-- 2. Create the Customers Table
CREATE TABLE customers (
                           id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                           tenant_id UNIQUEIDENTIFIER NOT NULL,
                           first_name NVARCHAR(100) NOT NULL,
                           last_name NVARCHAR(100) NOT NULL,
                           date_of_birth DATE NOT NULL,
                           national_id NVARCHAR(50),

    -- Status will track where they are in the ETL pipeline
                           status NVARCHAR(50) DEFAULT 'PROCESSING',
                           risk_tier NVARCHAR(20) DEFAULT 'UNKNOWN',

                           created_at DATETIME2 DEFAULT CURRENT_TIMESTAMP,

    -- Enforce the relationship: A customer MUST belong to a Bank
                           CONSTRAINT fk_customer_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- 3. Enterprise Indexing (The Secret to Multi-Tenant Speed)
-- Because every single query we write will filter by "tenant_id" to ensure
-- Bank A doesn't see Bank B's data, we MUST index this column.
CREATE INDEX idx_customers_tenant_id ON customers(tenant_id);

-- We also index national_id because the KYC engine will search by it frequently.
CREATE INDEX idx_customers_tenant_national_id ON customers(tenant_id, national_id);