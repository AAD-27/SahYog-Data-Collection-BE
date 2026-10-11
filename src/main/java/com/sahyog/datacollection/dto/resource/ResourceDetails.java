package com.sahyog.datacollection.dto.resource;

import java.math.BigDecimal;

public record ResourceDetails(
        Long resourceId,
        String resourceType,
        String resourceDetail,
        BigDecimal resourceValue,
        String resourceStartDate,
        String resourceEndDate,
        String resourceVerification,
        String ownershipType,
        BigDecimal ownershipSharePercent,
        String assetIdentifier,
        String acquisitionDate,
        String encumbranceStatus,
        BigDecimal outstandingLiabilityAmount) {
    public ResourceDetails(Long resourceId, String resourceType, String resourceDetail, BigDecimal resourceValue,
            String resourceStartDate, String resourceEndDate) {
        this(resourceId, resourceType, resourceDetail, resourceValue, resourceStartDate, resourceEndDate,
                null, null, null, null, null, null, null);
    }
}
