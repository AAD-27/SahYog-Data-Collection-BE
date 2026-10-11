package com.sahyog.datacollection.dto.disability;
import java.util.List;
public record DisabilityDetails(Long disabilityId, Long personId, String personName,
        List<String> disabilityTypes, String disabilityType, String otherDisabilityDetails,
        String proofOfDisability, String disabilityVerification, String disabilityStartDate, boolean sameAsBirthDate,
        String certificateNumber, String issuingAuthority, String certificateIssueDate,
        String certificateValidityType, String certificateExpiryDate, Integer disabilityPercentage) {
    public DisabilityDetails(Long disabilityId, Long personId, String personName, List<String> disabilityTypes,
            String disabilityType, String otherDisabilityDetails, String proofOfDisability,
            String disabilityStartDate, boolean sameAsBirthDate) {
        this(disabilityId, personId, personName, disabilityTypes, disabilityType, otherDisabilityDetails,
                proofOfDisability, proofOfDisability, disabilityStartDate, sameAsBirthDate,
                null, null, null, null, null, null);
    }
}
