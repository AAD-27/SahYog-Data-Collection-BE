package com.sahyog.datacollection.dto.expense;
import java.math.BigDecimal;
public record ExpenseDetails(Long expenseId, String expenseType, String expenseDetail, String expenseFrequency,
        BigDecimal expenseAmount, String expenseStartDate, String expenseEndDate, String expenseVerification,
        String serviceProviderName, String expenseReferenceNumber, String paidBy, String paymentMode,
        String billingPeriodFrom, String billingPeriodTo) {
    public ExpenseDetails(Long expenseId, String expenseType, String expenseDetail, String expenseFrequency,
            BigDecimal expenseAmount, String expenseStartDate, String expenseEndDate) {
        this(expenseId, expenseType, expenseDetail, expenseFrequency, expenseAmount, expenseStartDate, expenseEndDate,
                null, null, null, null, null, null, null);
    }
}
