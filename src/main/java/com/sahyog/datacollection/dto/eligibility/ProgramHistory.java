package com.sahyog.datacollection.dto.eligibility;
import java.util.List;
public record ProgramHistory(boolean hasApplicationUnderReview, boolean hasApprovedBenefit,
                             List<Long> benefitPersonIds) { }
