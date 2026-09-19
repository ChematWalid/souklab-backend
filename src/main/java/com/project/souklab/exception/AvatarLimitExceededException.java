package com.project.souklab.exception;

import com.project.souklab.dto.common.ApiErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user attempts to upload an avatar that would exceed the maximum gallery quota of 10 avatars.
 */
public class AvatarLimitExceededException extends AppException {

    /**
     * Constructs a new AvatarLimitExceededException with the specified explanatory message.
     *
     * @param message human-readable description of the quota violation
     */
    public AvatarLimitExceededException(String message) {
        super(HttpStatus.CONFLICT, ApiErrorCode.AVATAR_LIMIT_EXCEEDED, message);
    }
}
