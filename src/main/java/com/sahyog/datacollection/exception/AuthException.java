package com.sahyog.datacollection.exception;

public class AuthException extends RuntimeException {
    private final int status;
    private final String errorCode;
    public AuthException(int status, String errorCode, String message) {
        super(message); this.status = status; this.errorCode = errorCode;
    }
    public int status() { return status; }
    public String errorCode() { return errorCode; }
}
