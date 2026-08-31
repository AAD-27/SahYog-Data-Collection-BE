package com.sahyog.datacollection.service;
import com.sahyog.datacollection.dto.income.*;
public interface IncomeDetailsService {
    IncomeDetailsInitializeResponse initialize(IncomeDetailsInitializeRequest request);
    IncomeDetailsNavigationResponse next(IncomeDetailsSaveRequest request);
    IncomeDetailsNavigationResponse previous(IncomeDetailsSaveRequest request);
}
