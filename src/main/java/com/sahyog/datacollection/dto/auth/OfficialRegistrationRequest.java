package com.sahyog.datacollection.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OfficialRegistrationRequest(
        @NotBlank(message = "firstName is required") String firstName,
        String middleName,
        @NotBlank(message = "lastName is required") String lastName,
        @NotBlank(message = "employeeId is required") String employeeId,
        @NotBlank(message = "officialEmail is required") @Email(message = "officialEmail must be valid") String officialEmail,
        @NotBlank(message = "department is required") String department,
        @NotBlank(message = "designation is required") String designation,
        @NotBlank(message = "password is required") @Size(min = 8, message = "password must contain at least 8 characters") String password,
        @NotBlank(message = "confirmPassword is required") String confirmPassword) { }
