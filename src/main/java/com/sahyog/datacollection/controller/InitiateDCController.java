package com.sahyog.datacollection.controller;

import com.sahyog.datacollection.dto.ApplicationLookupResponse;
import com.sahyog.datacollection.dto.InitiateDCRequest;
import com.sahyog.datacollection.dto.InitiateDCResponse;
import com.sahyog.datacollection.service.InitiateDCService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/initiate-dc")
public class InitiateDCController {
    private final InitiateDCService service;

    public InitiateDCController(InitiateDCService service) {
        this.service = service;
    }

    @PostMapping("/lookup")
    public ResponseEntity<ApplicationLookupResponse> lookup(@RequestBody InitiateDCRequest request) {
        return ResponseEntity.ok(service.lookup(request.appOrCaseNum()));
    }

    @PostMapping("/continue")
    public ResponseEntity<InitiateDCResponse> initiate(@RequestBody InitiateDCRequest request) {
        return ResponseEntity.ok(service.initiate(request.appOrCaseNum()));
    }
}
