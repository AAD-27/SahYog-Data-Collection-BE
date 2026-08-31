package com.sahyog.datacollection.dto.applicant;

public record ApplicantDetailsInitializeResponse(
        String appOrCaseNum,
        String pageId,
        String firstName,
        String middleName,
        String lastName,
        String mobileNumber,
        String emailAddress,
        String applicationDate,
        boolean found) { }
