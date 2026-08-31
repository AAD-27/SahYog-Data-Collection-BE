package com.sahyog.datacollection.dto.program;

public record ProgramNavigationResponse(
        String appOrCaseNum,
        String status,
        String currentPageId,
        String destinationPageId) { }
