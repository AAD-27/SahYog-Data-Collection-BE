package com.sahyog.datacollection.controller;
import com.sahyog.datacollection.dto.disability.*;
import com.sahyog.datacollection.service.DisabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/disabilities")
public class DisabilityController {
    private final DisabilityService service;
    public DisabilityController(DisabilityService service){this.service=service;}
    @PostMapping("/summary/initialize") public ResponseEntity<DisabilitySummaryInitializeResponse> summaryInitialize(@RequestBody DisabilitySummaryInitializeRequest r){return ResponseEntity.ok(service.initializeSummary(r));}
    @PostMapping("/summary/next") public ResponseEntity<DisabilitySummaryNavigationResponse> summaryNext(@RequestBody DisabilitySummaryNavigationRequest r){return ResponseEntity.ok(service.summaryNext(r));}
    @PostMapping("/summary/previous") public ResponseEntity<DisabilitySummaryNavigationResponse> summaryPrevious(@RequestBody DisabilitySummaryNavigationRequest r){return ResponseEntity.ok(service.summaryPrevious(r));}
    @PostMapping("/summary/add-new") public ResponseEntity<DisabilitySummaryNavigationResponse> addNew(@RequestBody DisabilitySummaryNavigationRequest r){return ResponseEntity.ok(service.addNew(r));}
    @PostMapping("/summary/edit") public ResponseEntity<DisabilitySummaryNavigationResponse> edit(@RequestBody DisabilityRowRequest r){return ResponseEntity.ok(service.edit(r));}
    @PostMapping("/summary/delete") public ResponseEntity<DisabilityDeleteResponse> delete(@RequestBody DisabilityRowRequest r){return ResponseEntity.ok(service.delete(r));}
    @PostMapping("/details/initialize") public ResponseEntity<DisabilityDetailsInitializeResponse> detailsInitialize(@RequestBody DisabilityDetailsInitializeRequest r){return ResponseEntity.ok(service.initializeDetails(r));}
    @PostMapping("/details/next") public ResponseEntity<DisabilityDetailsNavigationResponse> detailsNext(@RequestBody DisabilityDetailsSaveRequest r){return ResponseEntity.ok(service.detailsNext(r));}
    @PostMapping("/details/previous") public ResponseEntity<DisabilityDetailsNavigationResponse> detailsPrevious(@RequestBody DisabilityDetailsSaveRequest r){return ResponseEntity.ok(service.detailsPrevious(r));}
}
