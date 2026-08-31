package com.sahyog.datacollection.dto.person;
import java.util.List;
public record PersonSummaryInitializeResponse(String appOrCaseNum, String pageId,
        List<PersonSummaryItem> persons, boolean found) { }
