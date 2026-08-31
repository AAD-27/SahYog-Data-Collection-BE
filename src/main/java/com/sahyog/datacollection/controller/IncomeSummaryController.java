package com.sahyog.datacollection.controller;
import com.sahyog.datacollection.dto.income.*;
import com.sahyog.datacollection.service.IncomeSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/income-summary")
public class IncomeSummaryController {
    private final IncomeSummaryService service;
    public IncomeSummaryController(IncomeSummaryService service) { this.service = service; }
    @PostMapping("/initialize")
    public ResponseEntity<IncomeSummaryInitializeResponse> initialize(@RequestBody IncomeSummaryInitializeRequest request) {
        return ResponseEntity.ok(service.initialize(request));
    }
    @PostMapping("/next")
    public ResponseEntity<IncomeSummaryNavigationResponse> next(@RequestBody IncomeSummaryNavigationRequest request) {
        return ResponseEntity.ok(service.next(request));
    }
    @PostMapping("/previous")
    public ResponseEntity<IncomeSummaryNavigationResponse> previous(@RequestBody IncomeSummaryNavigationRequest request) {
        return ResponseEntity.ok(service.previous(request));
    }
    @PostMapping("/add-new")
    public ResponseEntity<IncomeSummaryNavigationResponse> addNew(@RequestBody IncomeSummaryNavigationRequest request) {
        return ResponseEntity.ok(service.addNew(request));
    }
    @PostMapping("/edit")
    public ResponseEntity<IncomeSummaryNavigationResponse> edit(@RequestBody IncomeEditRequest request) {
        return ResponseEntity.ok(service.edit(request));
    }
    @PostMapping("/delete")
    public ResponseEntity<IncomeDeleteResponse> delete(@RequestBody IncomeDeleteRequest request) {
        return ResponseEntity.ok(service.delete(request));
    }
}
