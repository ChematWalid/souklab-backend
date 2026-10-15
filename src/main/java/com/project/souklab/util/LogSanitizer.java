package com.project.souklab.util;

/**
 * Utility to sanitize untrusted user inputs before writing to log streams,
 * preventing Log Injection / Log Forging (CWE-117).
 */
public final class LogSanitizer {

    private LogSanitizer() { }

    /**
     * Replaces carriage returns and line feeds with underscores to prevent log forging.
     *
     * @param value untrusted input value
     * @return sanitized string safe for logging
     */
    public static String sanitize(String value) {
        if (value == null) {
            return "null";
        }
        return value.replaceAll("[\r\n]", "_");
    }

    /**
     * Sanitizes arbitrary object representations by converting through String.valueOf.
     *
     * @param value untrusted object
     * @return sanitized string safe for logging
     */
    public static String sanitize(Object value) {
        if (value == null) {
            return "null";
        }
        return sanitize(String.valueOf(value));
    }
}
