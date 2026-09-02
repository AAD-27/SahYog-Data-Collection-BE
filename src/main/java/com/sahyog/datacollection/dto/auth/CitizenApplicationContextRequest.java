package com.sahyog.datacollection.dto.auth;

import jakarta.validation.constraints.Pattern;

public record CitizenApplicationContextRequest(
        @Pattern(regexp = "\\d{12}", message = "aadharNumber must contain exactly 12 digits") String aadharNumber,
        @Pattern(regexp = "\\d{10}", message = "mobileNumber must contain exactly 10 digits") String mobileNumber) { }
