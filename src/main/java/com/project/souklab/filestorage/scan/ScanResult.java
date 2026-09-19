package com.project.souklab.filestorage.scan;

import com.project.souklab.model.EnumValue;

/**
 * Encapsulates the result of an antivirus scan operation.
 * Distinguishes between clean files, infected files (with malware signature),
 * and scan failures or communication errors.
 */
public record ScanResult(
        Status status,
        String virusName,
        String message
) {

    /**
     * Outcome status of a scan operation.
     */
    public enum Status implements EnumValue {
        CLEAN,
        INFECTED,
        ERROR
    }

    /**
     * Creates a clean scan result indicating no malware was detected.
     *
     * @return clean ScanResult
     */
    public static ScanResult clean() {
        return new ScanResult(Status.CLEAN, null, null);
    }

    /**
     * Creates an infected scan result indicating malware was detected.
     *
     * @param virusName name of the detected malware signature
     * @return infected ScanResult
     */
    public static ScanResult infected(String virusName) {
        return new ScanResult(Status.INFECTED, virusName, "Malware signature detected: " + virusName);
    }

    /**
     * Creates an error scan result indicating a communication or scanner failure.
     *
     * @param message description of the failure
     * @return error ScanResult
     */
    public static ScanResult error(String message) {
        return new ScanResult(Status.ERROR, null, message);
    }

    /**
     * Returns true if the file was scanned and found to be clean.
     *
     * @return true if clean
     */
    public boolean isClean() {
        return status == Status.CLEAN;
    }

    /**
     * Returns true if malware was detected in the file.
     *
     * @return true if infected
     */
    public boolean isInfected() {
        return status == Status.INFECTED;
    }

    /**
     * Returns true if an error occurred during scanning.
     *
     * @return true if error
     */
    public boolean isError() {
        return status == Status.ERROR;
    }
}
