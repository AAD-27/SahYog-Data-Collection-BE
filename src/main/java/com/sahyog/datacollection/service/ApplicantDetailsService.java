package com.sahyog.datacollection.service;

import com.sahyog.datacollection.dto.applicant.ApplicantDetailsInitializeRequest;
import com.sahyog.datacollection.dto.applicant.ApplicantDetailsInitializeResponse;
import com.sahyog.datacollection.dto.applicant.ApplicantDetailsNavigationResponse;
import com.sahyog.datacollection.dto.applicant.ApplicantDetailsSaveRequest;

public interface ApplicantDetailsService {
    ApplicantDetailsInitializeResponse initialize(ApplicantDetailsInitializeRequest request);
    ApplicantDetailsNavigationResponse next(ApplicantDetailsSaveRequest request);
    ApplicantDetailsNavigationResponse previous(ApplicantDetailsSaveRequest request);
}
