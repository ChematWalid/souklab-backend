package com.project.souklab.util;

import java.security.SecureRandom;

public final class CodeGeneratorUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CodeGeneratorUtil() {
    }

    /**
     * Generates a cryptographically secure random numeric code with the specified number of digits.
     * The returned code is guaranteed to be zero-padded to match the exact length requested.
     *
     * @param digits the number of digits required (e.g., 6)
     * @return a zero-padded numeric string of length digits
     */
    public static String generateNumericCode(int digits) {
        if (digits <= 0 || digits > 10) {
            throw new IllegalArgumentException("Digits must be between 1 and 10");
        }
        int bound = (int) Math.pow(10, (double) digits);
        int number = SECURE_RANDOM.nextInt(bound);
        String str = Integer.toString(number);
        if (str.length() < digits) {
            return "0".repeat(digits - str.length()) + str;
        }
        return str;
    }
}
