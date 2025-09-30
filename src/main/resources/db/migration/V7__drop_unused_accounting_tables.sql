-- V7: drop unused accounting tables created in V1
-- Order matters: drop dependent objects first.

-- If the index was created separately, drop it defensively.
DROP INDEX IF EXISTS idx_postings_account;

-- Drop child table first (it references journal_entries).
DROP TABLE IF EXISTS postings CASCADE;

-- Then drop parent table.
DROP TABLE IF EXISTS journal_entries CASCADE;