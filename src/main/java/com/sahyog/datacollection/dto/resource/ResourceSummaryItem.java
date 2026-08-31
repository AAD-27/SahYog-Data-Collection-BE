package com.sahyog.datacollection.dto.resource;

public record ResourceSummaryItem(
        Long resourceId,
        String resourceType,
        String resourceDetail,
        String resourceStartDate) { }
