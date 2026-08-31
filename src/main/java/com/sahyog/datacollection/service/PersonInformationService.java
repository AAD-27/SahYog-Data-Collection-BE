package com.sahyog.datacollection.service;

import com.sahyog.datacollection.dto.person.*;

public interface PersonInformationService {
    PersonSummaryInitializeResponse initializeSummary(PersonSummaryInitializeRequest request);
    PersonSummaryNavigationResponse summaryNext(PersonSummaryNavigationRequest request);
    PersonSummaryNavigationResponse summaryPrevious(PersonSummaryNavigationRequest request);
    PersonSummaryNavigationResponse addNew(PersonSummaryNavigationRequest request);
    PersonSummaryNavigationResponse edit(PersonRowRequest request);
    PersonDeleteResponse delete(PersonRowRequest request);
    PersonInitializeResponse initialize(PersonInitializeRequest request);
    PersonNavigationResponse next(PersonSaveRequest request);
    PersonNavigationResponse previous(PersonSaveRequest request);
}
