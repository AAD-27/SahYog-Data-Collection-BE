package com.sahyog.datacollection.dto.expense;
import java.math.BigDecimal;
public record ExpenseDetails(Long expenseId, String expenseType, String expenseDetail, String expenseFrequency,
        BigDecimal expenseAmount, String expenseStartDate, String expenseEndDate) { }
