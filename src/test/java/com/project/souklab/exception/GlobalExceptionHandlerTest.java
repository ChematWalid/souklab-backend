package com.project.souklab.exception;

import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import com.project.souklab.filestorage.exception.StorageException;
import com.project.souklab.filestorage.exception.VirusDetectedException;
import com.project.souklab.filestorage.exception.VirusScanException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsDomainSecurityAndInfrastructureExceptions() {
        assertStatus(handler.handleAppException(new BadRequestException("bad")), HttpStatus.BAD_REQUEST);
        assertStatus(handler.handleStorageException(new StorageException("storage")), HttpStatus.INTERNAL_SERVER_ERROR);
        assertStatus(handler.handleVirusDetectedException(new VirusDetectedException("EICAR")), HttpStatus.UNPROCESSABLE_CONTENT);
        assertStatus(handler.handleVirusScanException(new VirusScanException("scanner")), HttpStatus.SERVICE_UNAVAILABLE);
        assertStatus(handler.handleAccessDeniedException(new AccessDeniedException("denied")), HttpStatus.FORBIDDEN);
        assertStatus(handler.handleAuthenticationException(new BadCredentialsException("bad credentials")), HttpStatus.UNAUTHORIZED);
        assertStatus(handler.handleIllegalArgumentException(new IllegalArgumentException("bad argument")), HttpStatus.BAD_REQUEST);
        assertStatus(handler.handleGenericException(new IllegalStateException("unexpected")), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void mapsHttpRequestErrors() {
        assertStatus(handler.handleMissingServletRequestParameterException(
                new MissingServletRequestParameterException("page", "int")), HttpStatus.BAD_REQUEST);
        assertStatus(handler.handleHttpMediaTypeNotSupportedException(
                new HttpMediaTypeNotSupportedException("application/xml")), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertStatus(handler.handleHttpRequestMethodNotSupportedException(
                new HttpRequestMethodNotSupportedException("PATCH")), HttpStatus.METHOD_NOT_ALLOWED);
        assertStatus(handler.handleMaxUploadSizeExceededException(
                new MaxUploadSizeExceededException(1024)), HttpStatus.CONTENT_TOO_LARGE);
    }

    @Test
    void mapsValidationAndMalformedRequestErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "name", "required"),
                new FieldError("request", "name", null)));
        BindException bindException = new BindException(bindingResult);
        assertThat(handler.handleValidationException(bindException).getBody().getErrors())
                .containsEntry("name", "required");

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("email");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("invalid");
        ConstraintViolationException constraint = new ConstraintViolationException(Set.of(violation));
        assertThat(handler.handleConstraintViolationException(constraint).getBody().getErrors())
                .containsEntry("email", "invalid");

        HttpMessageNotReadableException unreadable = new HttpMessageNotReadableException(
                "malformed", null, mock(HttpInputMessage.class));
        assertStatus(handler.handleHttpMessageNotReadableException(unreadable), HttpStatus.BAD_REQUEST);
        assertStatus(handler.handleNoResourceFoundException(mock(NoResourceFoundException.class)), HttpStatus.NOT_FOUND);
    }

    @Test
    void mapsTypeAndContentMetadataBranches() {
        MethodArgumentTypeMismatchException withType = mock(MethodArgumentTypeMismatchException.class);
        when(withType.getName()).thenReturn("page");
        Mockito.doReturn(Integer.class).when(withType).getRequiredType();
        assertStatus(handler.handleMethodArgumentTypeMismatchException(withType), HttpStatus.BAD_REQUEST);

        MethodArgumentTypeMismatchException withoutType = mock(MethodArgumentTypeMismatchException.class);
        when(withoutType.getName()).thenReturn("page");
        when(withoutType.getRequiredType()).thenReturn(null);
        assertStatus(handler.handleMethodArgumentTypeMismatchException(withoutType), HttpStatus.BAD_REQUEST);

        HttpMediaTypeNotSupportedException withoutContentType = mock(HttpMediaTypeNotSupportedException.class);
        when(withoutContentType.getContentType()).thenReturn(null);
        assertStatus(handler.handleHttpMediaTypeNotSupportedException(withoutContentType), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    private void assertStatus(ResponseEntity<?> response, HttpStatus status) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
    }
}
