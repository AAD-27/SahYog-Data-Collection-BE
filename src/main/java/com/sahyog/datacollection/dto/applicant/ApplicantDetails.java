package com.sahyog.datacollection.dto.applicant;

public record ApplicantDetails(
        String firstName,
        String middleName,
        String lastName,
        String mobileNumber,
        String emailAddress,
        String applicationDate) { }
