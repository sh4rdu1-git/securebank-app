CREATE TABLE beneficiaries (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id),
    account_number      VARCHAR(20) NOT NULL,
    beneficiary_name    VARCHAR(200) NOT NULL,
    bank_name           VARCHAR(100),
    nickname            VARCHAR(100),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, account_number)
);

CREATE INDEX idx_beneficiaries_user_id ON beneficiaries(user_id);