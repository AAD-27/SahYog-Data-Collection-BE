package com.sahyog.datacollection.controller;

import com.sahyog.datacollection.dto.address.AddressInitializeRequest;
import com.sahyog.datacollection.dto.address.AddressInitializeResponse;
import com.sahyog.datacollection.dto.address.AddressNavigationResponse;
import com.sahyog.datacollection.dto.address.AddressSaveRequest;
import com.sahyog.datacollection.service.AddressDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/address-details")
public class AddressDetailsController {
    private final AddressDetailsService service;

    public AddressDetailsController(AddressDetailsService service) {
        this.service = service;
    }

    @PostMapping("/initialize")
    public ResponseEntity<AddressInitializeResponse> initialize(@RequestBody AddressInitializeRequest request) {
        return ResponseEntity.ok(service.initialize(request));
    }

    @PostMapping("/next")
    public ResponseEntity<AddressNavigationResponse> next(@RequestBody AddressSaveRequest request) {
        return ResponseEntity.ok(service.next(request));
    }

    @PostMapping("/previous")
    public ResponseEntity<AddressNavigationResponse> previous(@RequestBody AddressSaveRequest request) {
        return ResponseEntity.ok(service.previous(request));
    }
}
