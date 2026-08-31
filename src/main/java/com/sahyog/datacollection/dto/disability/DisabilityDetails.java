package com.sahyog.datacollection.dto.disability;
import java.util.List;
public record DisabilityDetails(Long disabilityId, Long personId, String personName,
        List<String> disabilityTypes, String disabilityType, String otherDisabilityDetails,
        String proofOfDisability, String disabilityStartDate, boolean sameAsBirthDate) { }
