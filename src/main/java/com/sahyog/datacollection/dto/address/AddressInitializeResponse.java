package com.sahyog.datacollection.dto.address;

public record AddressInitializeResponse(
        String appOrCaseNum,
        String pageId,
        AddressPayload address,
        boolean found) { }
