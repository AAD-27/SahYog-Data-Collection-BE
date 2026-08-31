package com.sahyog.datacollection.service;

import com.sahyog.datacollection.dto.ApplicationLookupResponse;
import com.sahyog.datacollection.dto.InitiateDCResponse;

public interface InitiateDCService {
    ApplicationLookupResponse lookup(String applicationNumber);
    InitiateDCResponse initiate(String applicationNumber);
}
