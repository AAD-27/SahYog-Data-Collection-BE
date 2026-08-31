package com.sahyog.datacollection.dto.expense;
import java.util.List;
public record ExpenseSummaryInitializeResponse(String appOrCaseNum, String pageId,
        List<ExpenseSummaryItem> expenses, boolean found) { }
