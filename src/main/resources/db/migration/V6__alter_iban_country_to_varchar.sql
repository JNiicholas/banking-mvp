-- V6__alter_iban_country_to_varchar.sql
-- Align column type with Hibernate expectation (VARCHAR(2) instead of CHAR(2))
ALTER TABLE accounts
ALTER COLUMN iban_country TYPE VARCHAR(2)
  USING iban_country::varchar(2);