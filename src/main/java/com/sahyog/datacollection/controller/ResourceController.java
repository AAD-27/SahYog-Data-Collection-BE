package com.sahyog.datacollection.controller;
import com.sahyog.datacollection.dto.resource.*;
import com.sahyog.datacollection.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {
    private final ResourceService service;
    public ResourceController(ResourceService service) { this.service = service; }
    @PostMapping("/summary/initialize") public ResponseEntity<ResourceSummaryInitializeResponse> summaryInitialize(@RequestBody ResourceSummaryInitializeRequest r){return ResponseEntity.ok(service.initializeSummary(r));}
    @PostMapping("/summary/next") public ResponseEntity<ResourceSummaryNavigationResponse> summaryNext(@RequestBody ResourceSummaryNavigationRequest r){return ResponseEntity.ok(service.summaryNext(r));}
    @PostMapping("/summary/previous") public ResponseEntity<ResourceSummaryNavigationResponse> summaryPrevious(@RequestBody ResourceSummaryNavigationRequest r){return ResponseEntity.ok(service.summaryPrevious(r));}
    @PostMapping("/summary/add-new") public ResponseEntity<ResourceSummaryNavigationResponse> addNew(@RequestBody ResourceSummaryNavigationRequest r){return ResponseEntity.ok(service.addNew(r));}
    @PostMapping("/summary/edit") public ResponseEntity<ResourceSummaryNavigationResponse> edit(@RequestBody ResourceRowRequest r){return ResponseEntity.ok(service.edit(r));}
    @PostMapping("/summary/delete") public ResponseEntity<ResourceDeleteResponse> delete(@RequestBody ResourceRowRequest r){return ResponseEntity.ok(service.delete(r));}
    @PostMapping("/details/initialize") public ResponseEntity<ResourceDetailsInitializeResponse> detailsInitialize(@RequestBody ResourceDetailsInitializeRequest r){return ResponseEntity.ok(service.initializeDetails(r));}
    @PostMapping("/details/next") public ResponseEntity<ResourceDetailsNavigationResponse> detailsNext(@RequestBody ResourceDetailsSaveRequest r){return ResponseEntity.ok(service.detailsNext(r));}
    @PostMapping("/details/previous") public ResponseEntity<ResourceDetailsNavigationResponse> detailsPrevious(@RequestBody ResourceDetailsSaveRequest r){return ResponseEntity.ok(service.detailsPrevious(r));}
}
