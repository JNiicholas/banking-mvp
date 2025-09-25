CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE customers
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE accounts
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE journal_entries
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE postings
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE transactions
    ALTER COLUMN id SET DEFAULT gen_random_uuid();