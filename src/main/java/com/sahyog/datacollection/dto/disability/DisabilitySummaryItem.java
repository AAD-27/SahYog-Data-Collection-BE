package com.sahyog.datacollection.dto.disability;
import java.util.List;
public record DisabilitySummaryItem(Long disabilityId, Long personId, String personName, Integer age,
        List<String> disabilityTypes, String otherDisabilityDetails) { }
