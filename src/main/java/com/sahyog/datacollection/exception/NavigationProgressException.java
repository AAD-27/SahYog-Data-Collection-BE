package com.sahyog.datacollection.exception;

public class NavigationProgressException extends RuntimeException {
    private final int status;
    private final String errorCode;

    public NavigationProgressException(int status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public int status() {
        return status;
    }

    public String errorCode() {
        return errorCode;
    }
}
