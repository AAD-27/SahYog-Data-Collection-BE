package com.sahyog.datacollection.controller;
import com.sahyog.datacollection.dto.income.*;
import com.sahyog.datacollection.service.IncomeDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/income-details")
public class IncomeDetailsController {
    private final IncomeDetailsService service;
    public IncomeDetailsController(IncomeDetailsService service) { this.service = service; }
    @PostMapping("/initialize")
    public ResponseEntity<IncomeDetailsInitializeResponse> initialize(@RequestBody IncomeDetailsInitializeRequest request) {
        return ResponseEntity.ok(service.initialize(request));
    }
    @PostMapping("/next")
    public ResponseEntity<IncomeDetailsNavigationResponse> next(@RequestBody IncomeDetailsSaveRequest request) {
        return ResponseEntity.ok(service.next(request));
    }
    @PostMapping("/previous")
    public ResponseEntity<IncomeDetailsNavigationResponse> previous(@RequestBody IncomeDetailsSaveRequest request) {
        return ResponseEntity.ok(service.previous(request));
    }
}
