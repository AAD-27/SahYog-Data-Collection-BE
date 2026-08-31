package com.sahyog.datacollection.dto.resource;
import java.util.List;
public record ResourceSummaryInitializeResponse(String appOrCaseNum, String pageId,
        List<ResourceSummaryItem> resources, boolean found) { }
