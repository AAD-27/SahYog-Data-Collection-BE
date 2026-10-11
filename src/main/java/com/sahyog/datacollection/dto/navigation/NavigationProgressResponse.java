package com.sahyog.datacollection.dto.navigation;

import java.util.List;

public record NavigationProgressResponse(
        int status,
        String applicationNumber,
        List<NavigationStepProgress> steps) { }
