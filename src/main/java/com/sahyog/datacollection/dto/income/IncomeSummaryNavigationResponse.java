package com.sahyog.datacollection.dto.income;
public record IncomeSummaryNavigationResponse(String appOrCaseNum, String currentPageId,
        String destinationPageId, String mode, Long incomeId) { }
