package com.sahyog.datacollection.dto.disability;
public record DisabilityDetailsInitializeResponse(String appOrCaseNum, String pageId,
        DisabilityDetails disability, boolean found) { }
