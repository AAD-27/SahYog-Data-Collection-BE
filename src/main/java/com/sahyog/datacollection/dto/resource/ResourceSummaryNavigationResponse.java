package com.sahyog.datacollection.dto.resource;
public record ResourceSummaryNavigationResponse(String appOrCaseNum, String currentPageId,
        String destinationPageId, String mode, Long resourceId) { }
