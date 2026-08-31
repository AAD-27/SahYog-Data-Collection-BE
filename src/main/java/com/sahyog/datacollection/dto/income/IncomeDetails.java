package com.sahyog.datacollection.dto.income;
import java.math.BigDecimal;
public record IncomeDetails(Long incomeId, String employerName, String incomeType, String payFrequency,
        BigDecimal incomeAmount, BigDecimal approxYearlyIncome, EmployerAddress employerAddress,
        String incomeStartDate, String incomeEndDate) { }
