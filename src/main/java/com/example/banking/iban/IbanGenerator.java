package com.example.banking.iban;

import com.example.banking.dto.IbanResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * IBAN generator/validator for MVP. Supports a single country template (DE) with a fixed bank code
 * configured via application properties:
 *
 * <pre>
 * iban:
 *   country: DE
 *   bankCode: 50010517
 * </pre>
 *
 * For DE:
 *   BBAN = bankCode (8 digits) + accountNumber (10 digits)
 *   IBAN = country(2) + check(2) + BBAN
 *
 * Check digits are computed using ISO 13616 (mod-97) with character substitution A=10..Z=35.
 */
@Component
public class IbanGenerator {

    private final String country;   // e.g., "DE"
    private final String bankCode;  // e.g., "50010517"

    private final SecureRandom random = new SecureRandom();

    public IbanGenerator(@Value("${iban.country}") String country,
                         @Value("${iban.bankCode}") String bankCode) {
        this.country = Objects.requireNonNull(country, "iban.country").toUpperCase(Locale.ROOT);
        this.bankCode = Objects.requireNonNull(bankCode, "iban.bankCode");
        validateBankCode();
    }

    private void validateBankCode() {
        if (!country.equals("DE")) {
            throw new IllegalArgumentException("Only DE is supported in MVP. Provided country=" + country);
        }
        if (bankCode.length() != 8 || !bankCode.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("For DE, bankCode must be 8 digits");
        }
    }

    /**
     * Generate an IBAN for a given 10-digit account number (will be left-padded with zeros if shorter).
     * Returns both the normalized (no spaces, upper-case) and display (grouped by 4) variants.
     */
    public IbanResult generateForAccountNumber(String rawAccountNumber) {
        String acct = leftPadDigits(Objects.requireNonNull(rawAccountNumber, "accountNumber"), 10);
        if (!acct.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("accountNumber must contain only digits");
        }
        String bban = bankCode + acct; // 8 + 10 = 18
        String check = computeCheckDigits(country, bban);
        String normalized = country + check + bban;
        String display = prettyFormat(normalized);
        return new IbanResult(country, normalized, display);
    }

    /** Generate a new German IBAN using configured bankCode and a random 10-digit account number.
     *  Showcase - We do not care about uniqueness.
     * */
    public IbanResult generateNew() {
        long n = Math.floorMod(random.nextLong(), 1_000_000_0000L); // 0..9999999999
        String acct = String.format(Locale.ROOT, "%010d", n);
        return generateForAccountNumber(acct);
    }

    /**
     * Convenience: derive a deterministic 10-digit account number from a UUID (for seeding/backfills).
     */
    /** Deprecated for production: keeps deterministic backfill behavior. */
    @Deprecated
    public IbanResult generateForUuid(UUID id) {
        long lsb = id.getLeastSignificantBits();
        long positive = lsb == Long.MIN_VALUE ? 0L : Math.abs(lsb);
        long accountNumeric = positive % 1_000_000_0000L; // 10 digits
        String acct = String.format(Locale.ROOT, "%010d", accountNumeric);
        return generateForAccountNumber(acct);
    }

    /** Validate that an IBAN passes the mod-97 == 1 check after normalization. */
    public boolean isValid(String iban) {
        String normalized = normalize(iban);
        if (normalized == null || normalized.length() < 5) return false;
        String rearranged = normalized.substring(4) + normalized.substring(0, 4);
        String numeric = toNumericString(rearranged);
        return mod97(numeric) == 1;
    }

    /** Normalize: remove spaces, upper-case. */
    public static String normalize(String iban) {
        return iban == null ? null : iban.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    /** Pretty-print in groups of 4 characters. */
    public static String prettyFormat(String normalizedIban) {
        String s = normalize(normalizedIban);
        if (s == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && i % 4 == 0) sb.append(' ');
            sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    // --- Internal helpers ---

    private static String leftPadDigits(String raw, int len) {
        String digitsOnly = raw.replaceAll("\\D", "");
        if (digitsOnly.length() > len) {
            return digitsOnly.substring(digitsOnly.length() - len); // keep right-most len digits
        }
        return String.format(Locale.ROOT, "%0" + len + "d", Long.parseLong(digitsOnly));
    }

    /**
     * Compute ISO 13616 IBAN check digits (two-digit string) for given country and BBAN.
     */
    private static String computeCheckDigits(String country, String bban) {
        // Rearranged string: BBAN + country letters + "00"
        String rearranged = bban + country + "00";
        String numeric = toNumericString(rearranged);
        int rem = mod97(numeric);
        int check = 98 - rem;
        return check < 10 ? ("0" + check) : String.valueOf(check);
    }

    /** Convert letters to numbers (A=10..Z=35), keep digits, drop separators. */
    private static String toNumericString(String s) {
        StringBuilder out = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                out.append(c);
            } else if (c >= 'A' && c <= 'Z') {
                int val = c - 'A' + 10; // A=10 .. Z=35
                out.append(val);
            } else if (c >= 'a' && c <= 'z') {
                int val = c - 'a' + 10;
                out.append(val);
            } // ignore other characters
        }
        return out.toString();
    }

    /** Streaming mod-97 to avoid big integers. */
    private static int mod97(String digits) {
        int rem = 0;
        for (int i = 0; i < digits.length(); i++) {
            char ch = digits.charAt(i);
            if (ch < '0' || ch > '9') continue;
            int d = ch - '0';
            rem = (rem * 10 + d) % 97;
        }
        return rem;
    }

   }
