CREATE TABLE transactions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_number        VARCHAR(50) NOT NULL UNIQUE,
    source_account_id       UUID REFERENCES accounts(id),
    destination_account_id  UUID REFERENCES accounts(id),
    amount                  NUMERIC(19, 4) NOT NULL,
    currency                VARCHAR(3) NOT NULL DEFAULT 'INR',
    transaction_type        VARCHAR(30) NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description             VARCHAR(255),
    failure_reason          VARCHAR(500),
    initiated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at            TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_source_account ON transactions(source_account_id);
CREATE INDEX idx_transactions_dest_account ON transactions(destination_account_id);
CREATE INDEX idx_transactions_initiated_at ON transactions(initiated_at DESC);
CREATE INDEX idx_transactions_reference ON transactions(reference_number);