package com.sahyog.datacollection.dto.person;
public record PersonSummaryNavigationResponse(String appOrCaseNum, String currentPageId,
        String destinationPageId, String mode, Long personId) { }
