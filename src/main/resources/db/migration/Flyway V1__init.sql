CREATE TABLE customers (
                           id UUID PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           email VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE accounts (
                          id UUID PRIMARY KEY,
                          customer_id UUID NOT NULL REFERENCES customers(id),
                          balance NUMERIC(19,4) NOT NULL DEFAULT 0,
                          version BIGINT NOT NULL DEFAULT 0
);

-- Journal entries and postings for proper accounting (extensible)
CREATE TABLE journal_entries (
                                 id UUID PRIMARY KEY,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE postings (
                          id UUID PRIMARY KEY,
                          journal_id UUID NOT NULL REFERENCES journal_entries(id) ON DELETE CASCADE,
                          account_id UUID NOT NULL REFERENCES accounts(id),
                          amount NUMERIC(19,4) NOT NULL -- positive=debit, negative=credit (or vice versa)
);

-- Optional: enforce balanced entries (sum=0) with a deferred constraint via trigger
-- (Implement with a trigger function in V2 if you want hard guarantees.)

CREATE INDEX idx_accounts_customer ON accounts(customer_id);
CREATE INDEX idx_postings_account ON postings(account_id);