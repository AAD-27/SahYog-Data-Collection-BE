package com.sahyog.datacollection.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final int CUSTOM_VALIDATION_STATUS = 512;

    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomValidation(CustomValidationException exception) {
        return ResponseEntity.status(HttpStatusCode.valueOf(CUSTOM_VALIDATION_STATUS))
                .body(new ApiErrorResponse(CUSTOM_VALIDATION_STATUS, exception.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<AuthErrorResponse> handleAuth(AuthException exception) {
        return ResponseEntity.status(exception.status()).body(new AuthErrorResponse(
                Instant.now(), exception.status(), exception.errorCode(), exception.getMessage()));
    }

    @ExceptionHandler(NavigationProgressException.class)
    public ResponseEntity<NavigationProgressErrorResponse> handleNavigationProgress(
            NavigationProgressException exception) {
        return ResponseEntity.status(exception.status()).body(new NavigationProgressErrorResponse(
                exception.status(), exception.errorCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthErrorResponse> handleInvalidRequest(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst().map(error -> error.getDefaultMessage()).orElse("Invalid request");
        return ResponseEntity.badRequest().body(new AuthErrorResponse(
                Instant.now(), 400, "VALIDATION_ERROR", message));
    }
}
