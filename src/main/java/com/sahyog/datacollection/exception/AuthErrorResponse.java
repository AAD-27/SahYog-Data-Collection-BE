package com.sahyog.datacollection.exception;

import java.time.Instant;

public record AuthErrorResponse(Instant timestamp, int status, String errorCode, String message) { }
