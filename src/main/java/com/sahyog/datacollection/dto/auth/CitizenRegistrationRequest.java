package com.sahyog.datacollection.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CitizenRegistrationRequest(
        @NotBlank(message="firstName is required") String firstName,
        String middleName,
        @NotBlank(message="lastName is required") String lastName,
        @NotBlank(message="mobileNumber is required") @Pattern(regexp="\\d{10}", message="mobileNumber must contain exactly 10 digits") String mobileNumber,
        @Email(message="email must be valid") String email,
        @NotBlank(message="aadharNumber is required") @Pattern(regexp="\\d{12}", message="aadharNumber must contain exactly 12 digits") String aadharNumber,
        @NotBlank(message="password is required") @Size(min=8, message="password must contain at least 8 characters") String password,
        @NotBlank(message="confirmPassword is required") String confirmPassword) { }
