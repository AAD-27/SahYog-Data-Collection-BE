package com.sahyog.datacollection.dto.disability;
public record DisabilityDetailsNavigationResponse(String appOrCaseNum, Long disabilityId, String status,
        String currentPageId, String destinationPageId) { }
