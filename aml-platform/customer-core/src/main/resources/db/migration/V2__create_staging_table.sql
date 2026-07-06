CREATE TABLE stg_customer_upload (
                                     id BIGINT IDENTITY(1,1) PRIMARY KEY,
                                     customer_id VARCHAR(50),
                                     country_code VARCHAR(2),
                                     risk_score INT,
                                     ingested_at DATETIME2 DEFAULT CURRENT_TIMESTAMP
);