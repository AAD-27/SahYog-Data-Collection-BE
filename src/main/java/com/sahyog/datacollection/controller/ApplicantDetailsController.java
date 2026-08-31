package com.sahyog.datacollection.controller;

import com.sahyog.datacollection.dto.applicant.ApplicantDetailsInitializeRequest;
import com.sahyog.datacollection.dto.applicant.ApplicantDetailsInitializeResponse;
import com.sahyog.datacollection.dto.applicant.ApplicantDetailsNavigationResponse;
import com.sahyog.datacollection.dto.applicant.ApplicantDetailsSaveRequest;
import com.sahyog.datacollection.service.ApplicantDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/applicant-details")
public class ApplicantDetailsController {
    private final ApplicantDetailsService service;

    public ApplicantDetailsController(ApplicantDetailsService service) {
        this.service = service;
    }

    @PostMapping("/initialize")
    public ResponseEntity<ApplicantDetailsInitializeResponse> initialize(
            @RequestBody ApplicantDetailsInitializeRequest request) {
        return ResponseEntity.ok(service.initialize(request));
    }

    @PostMapping("/next")
    public ResponseEntity<ApplicantDetailsNavigationResponse> next(@RequestBody ApplicantDetailsSaveRequest request) {
        return ResponseEntity.ok(service.next(request));
    }

    @PostMapping("/previous")
    public ResponseEntity<ApplicantDetailsNavigationResponse> previous(@RequestBody ApplicantDetailsSaveRequest request) {
        return ResponseEntity.ok(service.previous(request));
    }
}
