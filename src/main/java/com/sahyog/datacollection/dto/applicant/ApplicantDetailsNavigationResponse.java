package com.sahyog.datacollection.dto.applicant;

public record ApplicantDetailsNavigationResponse(
        String appOrCaseNum,
        String status,
        String currentPageId,
        String destinationPageId) { }
