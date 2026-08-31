package com.sahyog.datacollection.service;
import com.sahyog.datacollection.dto.disability.*;
public interface DisabilityService {
    DisabilitySummaryInitializeResponse initializeSummary(DisabilitySummaryInitializeRequest request);
    DisabilitySummaryNavigationResponse summaryNext(DisabilitySummaryNavigationRequest request);
    DisabilitySummaryNavigationResponse summaryPrevious(DisabilitySummaryNavigationRequest request);
    DisabilitySummaryNavigationResponse addNew(DisabilitySummaryNavigationRequest request);
    DisabilitySummaryNavigationResponse edit(DisabilityRowRequest request);
    DisabilityDeleteResponse delete(DisabilityRowRequest request);
    DisabilityDetailsInitializeResponse initializeDetails(DisabilityDetailsInitializeRequest request);
    DisabilityDetailsNavigationResponse detailsNext(DisabilityDetailsSaveRequest request);
    DisabilityDetailsNavigationResponse detailsPrevious(DisabilityDetailsSaveRequest request);
}
