package com.sahyog.datacollection.dto.expense;
public record ExpenseDetailsInitializeResponse(String appOrCaseNum, String pageId,
        ExpenseDetails expense, boolean found) { }
