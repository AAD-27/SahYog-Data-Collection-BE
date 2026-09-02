package com.sahyog.datacollection.dto.auth;

public record OfficialRegistrationResponse(Long userId, String employeeId, String profileType,
                                           String role, String message, String approvalStatus) { }
