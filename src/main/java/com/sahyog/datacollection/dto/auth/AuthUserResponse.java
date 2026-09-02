package com.sahyog.datacollection.dto.auth;

public record AuthUserResponse(Long userId, String profileType, String employeeId, String citizenId,
                               String email, String fullName, String role, String mobileNumber) { }
