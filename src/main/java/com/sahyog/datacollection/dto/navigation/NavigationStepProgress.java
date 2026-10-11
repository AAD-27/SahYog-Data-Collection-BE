package com.sahyog.datacollection.dto.navigation;

public record NavigationStepProgress(
        String pageId,
        String status,
        boolean hasData,
        int recordCount,
        String lastSavedAt) { }
