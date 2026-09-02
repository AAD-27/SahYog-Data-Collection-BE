package com.sahyog.datacollection.dto.auth;

public record LoginResponse(String accessToken, String refreshToken, String tokenType,
                            long expiresIn, AuthUserResponse user) { }
