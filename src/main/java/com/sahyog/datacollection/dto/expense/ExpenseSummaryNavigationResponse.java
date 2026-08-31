package com.sahyog.datacollection.dto.expense;
public record ExpenseSummaryNavigationResponse(String appOrCaseNum, String currentPageId,
        String destinationPageId, String mode, Long expenseId) { }
