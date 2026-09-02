package com.sahyog.datacollection.dto.auth;

public record CitizenApplicationContextResponse(
        String citizenId,
        String fullName,
        String aadharNumber,
        String mobileNumber,
        String applicationNumber,
        String caseNumber,
        String applicationStatus,
        String currentModule,
        String currentModuleName,
        String currentPageId,
        String currentPageName,
        boolean found) { }
