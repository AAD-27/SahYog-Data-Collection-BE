package com.sahyog.datacollection.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message="profileType is required") String profileType,
        @NotBlank(message="loginId is required") String loginId) { }
