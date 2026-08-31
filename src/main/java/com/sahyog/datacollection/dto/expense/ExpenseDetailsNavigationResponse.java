package com.sahyog.datacollection.dto.expense;
public record ExpenseDetailsNavigationResponse(String appOrCaseNum, Long expenseId, String status,
        String currentPageId, String destinationPageId) { }
