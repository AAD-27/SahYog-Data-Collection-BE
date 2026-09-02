package com.sahyog.datacollection.dto.auth;

public record CitizenRegistrationResponse(Long userId, String citizenId, String profileType,
                                          String message, boolean verificationRequired) { }
