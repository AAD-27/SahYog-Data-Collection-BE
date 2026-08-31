package com.sahyog.datacollection.dto.disability;
public record DisabilitySummaryNavigationResponse(String appOrCaseNum, String currentPageId,
        String destinationPageId, String mode, Long disabilityId) { }
