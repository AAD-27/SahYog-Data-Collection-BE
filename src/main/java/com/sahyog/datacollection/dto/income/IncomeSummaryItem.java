package com.sahyog.datacollection.dto.income;
import java.time.LocalDate;
public record IncomeSummaryItem(Long incomeId, String incomeType, String employerName,
        LocalDate incomeStartDate, String payFrequency) { }
