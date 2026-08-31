package com.sahyog.datacollection.dto.disability;
import java.util.List;
public record DisabilitySummaryInitializeResponse(String appOrCaseNum, String pageId,
        List<DisabilitySummaryItem> disabilities, boolean found) { }
