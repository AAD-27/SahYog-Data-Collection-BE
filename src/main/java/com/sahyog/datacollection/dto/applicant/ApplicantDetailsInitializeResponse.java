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
        Boolean isIndianResident,
        String employmentStatus,
        Boolean isSoleEarningMember,
        java.util.List<String> medicalConditions,
        String otherMedicalCondition,
        Boolean isMajorOrChronic,
        boolean found) { }
