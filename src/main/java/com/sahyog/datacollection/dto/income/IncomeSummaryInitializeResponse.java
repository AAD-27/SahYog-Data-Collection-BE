package com.sahyog.datacollection.dto.income;
import java.util.List;
public record IncomeSummaryInitializeResponse(String appOrCaseNum, String pageId,
        List<IncomeSummaryItem> incomes, boolean found) { }
