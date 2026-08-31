package com.sahyog.datacollection.dto.resource;
public record ResourceDetailsNavigationResponse(String appOrCaseNum, Long resourceId, String status,
        String currentPageId, String destinationPageId) { }
