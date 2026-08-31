package com.sahyog.datacollection.service;

import com.sahyog.datacollection.dto.program.ProgramInitializeRequest;
import com.sahyog.datacollection.dto.program.ProgramInitializeResponse;
import com.sahyog.datacollection.dto.program.ProgramNavigationResponse;
import com.sahyog.datacollection.dto.program.ProgramSaveRequest;

public interface ProgramDetailsService {
    ProgramInitializeResponse initialize(ProgramInitializeRequest request);
    ProgramNavigationResponse next(ProgramSaveRequest request);
    ProgramNavigationResponse previous(ProgramSaveRequest request);
}
