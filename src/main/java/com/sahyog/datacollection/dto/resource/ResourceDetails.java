package com.sahyog.datacollection.dto.resource;

import java.math.BigDecimal;

public record ResourceDetails(
        Long resourceId,
        String resourceType,
        String resourceDetail,
        BigDecimal resourceValue,
        String resourceStartDate,
        String resourceEndDate) { }
