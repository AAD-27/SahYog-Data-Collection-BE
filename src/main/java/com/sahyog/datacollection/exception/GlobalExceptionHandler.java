package com.sahyog.datacollection.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final int CUSTOM_VALIDATION_STATUS = 512;

    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomValidation(CustomValidationException exception) {
        return ResponseEntity.status(HttpStatusCode.valueOf(CUSTOM_VALIDATION_STATUS))
                .body(new ApiErrorResponse(CUSTOM_VALIDATION_STATUS, exception.getMessage()));
    }
}
