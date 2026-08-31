package com.sahyog.datacollection.service;
import com.sahyog.datacollection.dto.income.*;
public interface IncomeSummaryService {
    IncomeSummaryInitializeResponse initialize(IncomeSummaryInitializeRequest request);
    IncomeSummaryNavigationResponse next(IncomeSummaryNavigationRequest request);
    IncomeSummaryNavigationResponse previous(IncomeSummaryNavigationRequest request);
    IncomeSummaryNavigationResponse addNew(IncomeSummaryNavigationRequest request);
    IncomeSummaryNavigationResponse edit(IncomeEditRequest request);
    IncomeDeleteResponse delete(IncomeDeleteRequest request);
}
