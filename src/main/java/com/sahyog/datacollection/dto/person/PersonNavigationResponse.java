package com.sahyog.datacollection.dto.person;

public record PersonNavigationResponse(
        String appOrCaseNum,
        String status,
        String currentPageId,
        String destinationPageId) { }
