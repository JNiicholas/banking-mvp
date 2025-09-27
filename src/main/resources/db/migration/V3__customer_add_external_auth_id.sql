-- V3__customer_add_external_auth_id.sql
ALTER TABLE customers
    ADD COLUMN external_auth_id UUID NULL;

-- Optional: track realm if you might have multiple realms now or later
ALTER TABLE customers
    ADD COLUMN external_auth_realm VARCHAR(64) NULL;

-- Enforce at most one customer per KC user (ignore NULLs)
CREATE UNIQUE INDEX ux_customer_external_auth
    ON customers(external_auth_id)
    WHERE external_auth_id IS NOT NULL;

-- Helpful lookup
CREATE INDEX ix_customer_external_auth_realm
    ON customers(external_auth_realm);