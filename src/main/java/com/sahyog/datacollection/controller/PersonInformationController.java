package com.sahyog.datacollection.controller;

import com.sahyog.datacollection.dto.person.*;
import com.sahyog.datacollection.service.PersonInformationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/person-information")
public class PersonInformationController {
    private final PersonInformationService service;

    public PersonInformationController(PersonInformationService service) {
        this.service = service;
    }

    @PostMapping("/summary/initialize")
    public ResponseEntity<PersonSummaryInitializeResponse> summaryInitialize(@RequestBody PersonSummaryInitializeRequest request) {
        return ResponseEntity.ok(service.initializeSummary(request));
    }

    @PostMapping("/summary/next")
    public ResponseEntity<PersonSummaryNavigationResponse> summaryNext(@RequestBody PersonSummaryNavigationRequest request) {
        return ResponseEntity.ok(service.summaryNext(request));
    }

    @PostMapping("/summary/previous")
    public ResponseEntity<PersonSummaryNavigationResponse> summaryPrevious(@RequestBody PersonSummaryNavigationRequest request) {
        return ResponseEntity.ok(service.summaryPrevious(request));
    }

    @PostMapping("/summary/add-new")
    public ResponseEntity<PersonSummaryNavigationResponse> addNew(@RequestBody PersonSummaryNavigationRequest request) {
        return ResponseEntity.ok(service.addNew(request));
    }

    @PostMapping("/summary/edit")
    public ResponseEntity<PersonSummaryNavigationResponse> edit(@RequestBody PersonRowRequest request) {
        return ResponseEntity.ok(service.edit(request));
    }

    @PostMapping("/summary/delete")
    public ResponseEntity<PersonDeleteResponse> delete(@RequestBody PersonRowRequest request) {
        return ResponseEntity.ok(service.delete(request));
    }

    @PostMapping({"/details/initialize", "/initialize"})
    public ResponseEntity<PersonInitializeResponse> initialize(@RequestBody PersonInitializeRequest request) {
        return ResponseEntity.ok(service.initialize(request));
    }

    @PostMapping({"/details/next", "/next"})
    public ResponseEntity<PersonNavigationResponse> next(@RequestBody PersonSaveRequest request) {
        return ResponseEntity.ok(service.next(request));
    }

    @PostMapping({"/details/previous", "/previous"})
    public ResponseEntity<PersonNavigationResponse> previous(@RequestBody PersonSaveRequest request) {
        return ResponseEntity.ok(service.previous(request));
    }
}
