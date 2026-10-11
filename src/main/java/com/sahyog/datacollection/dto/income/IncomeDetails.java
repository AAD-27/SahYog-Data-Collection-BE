package com.sahyog.datacollection.dto.income;
import java.math.BigDecimal;
public record IncomeDetails(Long incomeId, String employerName, String incomeType, String payFrequency,
        BigDecimal incomeAmount, BigDecimal approxYearlyIncome, EmployerAddress employerAddress,
        String incomeStartDate, String incomeEndDate, String incomeVerification, String employmentCategory,
        String occupationDesignation, String incomeBasis, String paymentMode, String verificationReferenceNumber,
        String verificationDocumentDate) {
    public IncomeDetails(Long incomeId, String employerName, String incomeType, String payFrequency,
            BigDecimal incomeAmount, BigDecimal approxYearlyIncome, EmployerAddress employerAddress,
            String incomeStartDate, String incomeEndDate) {
        this(incomeId, employerName, incomeType, payFrequency, incomeAmount, approxYearlyIncome, employerAddress,
                incomeStartDate, incomeEndDate, null, null, null, null, null, null, null);
    }
}
