package com.sahyog.datacollection.dto.address;

public record AddressNavigationResponse(
        String appOrCaseNum,
        String status,
        String currentPageId,
        String destinationPageId) { }
