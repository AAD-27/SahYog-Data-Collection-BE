package com.sahyog.datacollection.dto.eligibility;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
public record EligibilitySnapshotResponse(String appOrCaseNum, String applicationNumber, LocalDate policyEffectiveDate,
        BigDecimal annualHouseholdIncome, BigDecimal monthlyHouseholdIncome, BigDecimal monthlyTotalExpenses,
        BigDecimal monthlyFoodExpenses, BigDecimal monthlyUtilityExpenses, BigDecimal monthlyElectricityExpense,
        BigDecimal monthlyWaterExpense, BigDecimal monthlyMedicalExpenses, Boolean isIndianResident,
        Integer applicantAge, String employmentStatus, Boolean isSoleEarningMember, BigDecimal totalResourceValue,
        boolean hasCertifiedDisability, Boolean hasMajorOrChronicMedicalCondition, List<EligibleChild> eligibleChildren,
        String occupancyType, Boolean responsibleForUtilityBills, List<String> requestedProgramCodes,
        Map<String, ProgramHistory> programHistory) { }
