

ALTER TABLE customers
    ADD COLUMN first_name VARCHAR(50),
    ADD COLUMN last_name VARCHAR(50);

-- Optionally migrate existing data: split name into first and last
UPDATE customers
SET first_name = split_part(name, ' ', 1),
    last_name = CASE WHEN array_length(string_to_array(name, ' '), 1) > 1 THEN substring(name from position(' ' in name) + 1) ELSE '' END
WHERE name IS NOT NULL;

ALTER TABLE customers
    DROP COLUMN name;