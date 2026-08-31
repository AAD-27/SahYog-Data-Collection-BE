package com.sahyog.datacollection.service;
import com.sahyog.datacollection.dto.expense.*;
public interface ExpenseService {
    ExpenseSummaryInitializeResponse initializeSummary(ExpenseSummaryInitializeRequest request);
    ExpenseSummaryNavigationResponse summaryNext(ExpenseSummaryNavigationRequest request);
    ExpenseSummaryNavigationResponse summaryPrevious(ExpenseSummaryNavigationRequest request);
    ExpenseSummaryNavigationResponse addNew(ExpenseSummaryNavigationRequest request);
    ExpenseSummaryNavigationResponse edit(ExpenseRowRequest request);
    ExpenseDeleteResponse delete(ExpenseRowRequest request);
    ExpenseDetailsInitializeResponse initializeDetails(ExpenseDetailsInitializeRequest request);
    ExpenseDetailsNavigationResponse detailsNext(ExpenseDetailsSaveRequest request);
    ExpenseDetailsNavigationResponse detailsPrevious(ExpenseDetailsSaveRequest request);
}
