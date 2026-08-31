package com.sahyog.datacollection.dto.applicant;

public record ApplicantDetailsSaveRequest(
        String appOrCaseNum,
        String pageId,
        ApplicantDetails applicantDetails) { }
