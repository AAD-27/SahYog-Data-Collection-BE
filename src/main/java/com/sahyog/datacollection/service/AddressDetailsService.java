package com.sahyog.datacollection.service;

import com.sahyog.datacollection.dto.address.AddressInitializeRequest;
import com.sahyog.datacollection.dto.address.AddressInitializeResponse;
import com.sahyog.datacollection.dto.address.AddressNavigationResponse;
import com.sahyog.datacollection.dto.address.AddressSaveRequest;

public interface AddressDetailsService {
    AddressInitializeResponse initialize(AddressInitializeRequest request);
    AddressNavigationResponse next(AddressSaveRequest request);
    AddressNavigationResponse previous(AddressSaveRequest request);
}
