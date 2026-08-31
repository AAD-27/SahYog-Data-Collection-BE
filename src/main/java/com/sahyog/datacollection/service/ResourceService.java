package com.sahyog.datacollection.service;
import com.sahyog.datacollection.dto.resource.*;
public interface ResourceService {
    ResourceSummaryInitializeResponse initializeSummary(ResourceSummaryInitializeRequest request);
    ResourceSummaryNavigationResponse summaryNext(ResourceSummaryNavigationRequest request);
    ResourceSummaryNavigationResponse summaryPrevious(ResourceSummaryNavigationRequest request);
    ResourceSummaryNavigationResponse addNew(ResourceSummaryNavigationRequest request);
    ResourceSummaryNavigationResponse edit(ResourceRowRequest request);
    ResourceDeleteResponse delete(ResourceRowRequest request);
    ResourceDetailsInitializeResponse initializeDetails(ResourceDetailsInitializeRequest request);
    ResourceDetailsNavigationResponse detailsNext(ResourceDetailsSaveRequest request);
    ResourceDetailsNavigationResponse detailsPrevious(ResourceDetailsSaveRequest request);
}
