package com.project.souklab.exception;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.ApiErrorCode;
import com.project.souklab.filestorage.exception.StorageException;
import com.project.souklab.filestorage.exception.VirusDetectedException;
import com.project.souklab.filestorage.exception.VirusScanException;
import com.project.souklab.util.SecurityUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String STORAGE_UNAVAILABLE_MESSAGE = "File storage is temporarily unavailable.";
    private static final String VIRUS_SCAN_UNAVAILABLE_MESSAGE = "File security scanning is temporarily unavailable.";

    /**
     * Handles all custom application exceptions (AppException and subclasses like
     * ResourceNotFoundException, ConflictException, ForbiddenException, UnauthorizedException, BadRequestException).
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        log.warn("AppException [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    /**
     * Handles malware detected during file upload antivirus scanning (422 Unprocessable Content).
     */
    @ExceptionHandler(VirusDetectedException.class)
    public ResponseEntity<ApiResponse<Void>> handleVirusDetectedException(VirusDetectedException ex) {
        log.warn("VirusDetectedException [VIRUS_DETECTED]: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiResponse.error(ApiErrorCode.VIRUS_DETECTED, ex.getMessage()));
    }

    /**
     * Handles failures in the antivirus scanning infrastructure under fail-closed policy (503 Service Unavailable).
     */
    @ExceptionHandler(VirusScanException.class)
    public ResponseEntity<ApiResponse<Void>> handleVirusScanException(VirusScanException ex) {
        log.error("VirusScanException [VIRUS_SCAN_UNAVAILABLE]: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(ApiErrorCode.VIRUS_SCAN_UNAVAILABLE, VIRUS_SCAN_UNAVAILABLE_MESSAGE));
    }

    /**
     * Handles file storage domain exceptions, mapping encapsulated status and error code to ApiResponse.
     */
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiResponse<Void>> handleStorageException(StorageException ex) {
        log.warn("StorageException [{}]: {}", ex.getErrorCode(), ex.getMessage());
        String responseMessage = ex.getStatus().is5xxServerError()
                ? STORAGE_UNAVAILABLE_MESSAGE
                : ex.getMessage();
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getErrorCode(), responseMessage));
    }

    /**
     * Handles Jakarta Bean Validation errors on @Valid request bodies and query models (422 Unprocessable Entity).
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidationException(BindException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage() != null ? error.getDefaultMessage() : "is invalid");
        }
        log.debug("Validation error: {}", fieldErrors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiResponse.validationError(fieldErrors));
    }

    /**
     * Handles constraint violation errors on method parameters (422 Unprocessable Entity).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            fieldErrors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage());
        }
        log.debug("ConstraintViolationException: {}", fieldErrors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiResponse.validationError(fieldErrors));
    }

    /**
     * Handles malformed or unparseable JSON payloads (400 Bad Request).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("HttpMessageNotReadableException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Malformed or unreadable request body"));
    }

    /**
     * Handles missing static/controller resources and unmatched routes (404 Not Found).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.debug("NoResourceFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("The requested resource was not found"));
    }

    /**
     * Handles unsupported HTTP methods (405 Method Not Allowed).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.debug("HttpRequestMethodNotSupportedException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("HTTP method not allowed"));
    }

    /**
     * Handles Spring Security access denied errors (403 Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        String username = SecurityUtils.getCurrentUsername();
        log.warn("AccessDeniedException for user [{}]: {}", username != null ? username : "anonymous", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ApiErrorCode.FORBIDDEN, "You do not have permission to perform this action."));
    }

    /**
     * Handles Spring Security authentication failures (401 Unauthorized).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("AuthenticationException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication failed: Invalid credentials or expired session."));
    }

    /**
     * Handles missing required request parameters (400 Bad Request).
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.warn("MissingServletRequestParameterException: {}", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Missing required request parameter: " + ex.getParameterName()));
    }

    /**
     * Handles type mismatch on path variables / query params (400 Bad Request).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid type";
        String message = String.format("Parameter '%s' should be of type %s.", paramName, expectedType);
        log.warn("MethodArgumentTypeMismatchException: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    /**
     * Handles unsupported media/content types (415 Unsupported Media Type).
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        log.warn("HttpMediaTypeNotSupportedException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error("Unsupported media type: " + ex.getContentType()));
    }

    /**
     * Handles file uploads exceeding maximum allowed size (413 Content Too Large).
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("MaxUploadSizeExceededException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(ApiResponse.error("Maximum upload size exceeded."));
    }

    /**
     * Handles illegal argument exceptions (400 Bad Request).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Last-resort fallback for unhandled exceptions (500 Internal Server Error).
     * Logs full stack trace server-side but returns a safe, non-leaky generic message to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled server exception occurred: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
    }
}
