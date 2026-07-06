ALTER TABLE stg_customer_upload ADD kyc_status VARCHAR(20) DEFAULT 'PENDING';
ALTER TABLE stg_customer_upload ADD kyc_notes VARCHAR(255);