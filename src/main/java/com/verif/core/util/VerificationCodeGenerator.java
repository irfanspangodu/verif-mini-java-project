package com.verif.core.util;

import java.security.SecureRandom;
import java.time.Year;

/**
 * Cryptographically sound utility class for generating collision-resistant,
 * high-entropy tracking codes and document hashes for the verification pipeline.
 */
public final class VerificationCodeGenerator {

    // Character alphabet for concise, human-friendly alphanumeric codes
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    
    // Secure random instance for cryptographically secure pseudo-random numbers
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Private constructor to prevent instantiation of static utility class.
     */
    private VerificationCodeGenerator() {
        // Enforce non-instantiability
    }

    /**
     * Generates a unique, standardized tracking code in the format:
     * VRF-{YEAR}-{6 RANDOM CHARACTERS} (e.g., VRF-2026-K92A1B).
     * 
     * @return Formatted alphanumeric tracking code
     */
    public static String generateTrackingCode() {
        // Step 1: Capture current Gregorian calendar year
        int currentYear = Year.now().getValue();

        // Step 2: Build 6 random alphanumeric characters
        StringBuilder randomSegment = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(ALPHABET.length());
            randomSegment.append(ALPHABET.charAt(randomIndex));
        }

        // Step 3: Assemble formatted string token
        return String.format("VRF-%d-%s", currentYear, randomSegment.toString());
    }

    /**
     * Generates a pseudo-document reference identifier for simulated document verification.
     * 
     * @return Simulated document reference hash
     */
    public static String generateDocumentReference() {
        StringBuilder ref = new StringBuilder("DOC-REF-");
        for (int i = 0; i < 5; i++) {
            ref.append(ALPHABET.charAt(SECURE_RANDOM.nextInt(ALPHABET.length())));
        }
        return ref.append("-SHA256").toString();
    }
}
