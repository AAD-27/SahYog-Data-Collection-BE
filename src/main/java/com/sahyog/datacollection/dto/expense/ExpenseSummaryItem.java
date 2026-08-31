package com.sahyog.datacollection.dto.expense;
public record ExpenseSummaryItem(Long expenseId, String expenseType, String expenseStartDate,
        String expenseDetail, String expenseFrequency) { }
