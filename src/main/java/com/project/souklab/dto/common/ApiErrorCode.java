package com.project.souklab.dto.common;

import com.project.souklab.model.EnumValue;

/** Closed set of standardized API error codes. */
public enum ApiErrorCode implements EnumValue {
    BAD_REQUEST,
    CONFLICT,
    FORBIDDEN,
    RESOURCE_NOT_FOUND,
    UNAUTHORIZED,
    AVATAR_LIMIT_EXCEEDED,
    VIRUS_DETECTED,
    VIRUS_SCAN_UNAVAILABLE,
    FILE_TOO_LARGE,
    FILE_NOT_FOUND,
    INVALID_FILENAME,
    UNSUPPORTED_FILE_TYPE,
    UNSUPPORTED_IMAGE_FORMAT,
    STORAGE_ERROR,
    TOO_MANY_REQUESTS
}
