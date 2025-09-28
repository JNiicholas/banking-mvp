-- V5__add_iban.sql
-- Add columns as NULLABLE first (safe when table has data), then backfill, then enforce NOT NULL + UNIQUE

ALTER TABLE accounts
    ADD COLUMN iban_country CHAR(2),
  ADD COLUMN iban_normalized VARCHAR(34),
  ADD COLUMN iban_display VARCHAR(42);

-- Backfill existing rows
-- Placeholder normalized IBAN: DE + 00 + UUID digits-only (padded to 30) — ensures uniqueness until real generator runs
UPDATE accounts a
SET
    iban_country = COALESCE(a.iban_country, 'DE'),
    iban_normalized = COALESCE(
            a.iban_normalized,
            UPPER('DE' || '00' || LPAD(REPLACE(a.id::text, '-', ''), 30, '0'))
                      ),
    iban_display = COALESCE(
            a.iban_display,
            TRIM(BOTH FROM regexp_replace(
                    UPPER(
                            COALESCE(
                                    a.iban_normalized,
                                    'DE' || '00' || LPAD(REPLACE(a.id::text, '-', ''), 30, '0')
                            )
                    ),
                    '(.{4})', '\1 ', 'g'
                           ))
                   );

-- Enforce constraints
ALTER TABLE accounts
    ALTER COLUMN iban_country SET NOT NULL,
ALTER COLUMN iban_normalized SET NOT NULL;

-- Make future inserts default to configured country (DE for MVP)
ALTER TABLE accounts
    ALTER COLUMN iban_country SET DEFAULT 'DE';

-- Uniqueness on normalized IBAN
CREATE UNIQUE INDEX IF NOT EXISTS ux_accounts_iban_normalized ON accounts (iban_normalized);

-- Basic checks (optional but helpful)
ALTER TABLE accounts
    ADD CONSTRAINT chk_accounts_iban_country CHECK (iban_country ~ '^[A-Z]{2}$'),
    ADD CONSTRAINT chk_accounts_iban_normalized CHECK (iban_normalized ~ '^[A-Z0-9]{15,34}$');

-- Note: Application code should generate true IBANs on creation (with valid check digits)
-- and set both iban_normalized (no spaces, uppercase) and optionally iban_display (grouped every 4 chars).