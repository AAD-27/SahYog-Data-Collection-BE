package com.sahyog.datacollection.dto.income;
public record IncomeDetailsNavigationResponse(String appOrCaseNum, Long incomeId, String status,
        String currentPageId, String destinationPageId) { }
