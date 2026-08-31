package com.sahyog.datacollection.controller;
import com.sahyog.datacollection.dto.expense.*;
import com.sahyog.datacollection.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {
    private final ExpenseService service;
    public ExpenseController(ExpenseService service) { this.service=service; }
    @PostMapping("/summary/initialize") public ResponseEntity<ExpenseSummaryInitializeResponse> summaryInitialize(@RequestBody ExpenseSummaryInitializeRequest r){return ResponseEntity.ok(service.initializeSummary(r));}
    @PostMapping("/summary/next") public ResponseEntity<ExpenseSummaryNavigationResponse> summaryNext(@RequestBody ExpenseSummaryNavigationRequest r){return ResponseEntity.ok(service.summaryNext(r));}
    @PostMapping("/summary/previous") public ResponseEntity<ExpenseSummaryNavigationResponse> summaryPrevious(@RequestBody ExpenseSummaryNavigationRequest r){return ResponseEntity.ok(service.summaryPrevious(r));}
    @PostMapping("/summary/add-new") public ResponseEntity<ExpenseSummaryNavigationResponse> addNew(@RequestBody ExpenseSummaryNavigationRequest r){return ResponseEntity.ok(service.addNew(r));}
    @PostMapping("/summary/edit") public ResponseEntity<ExpenseSummaryNavigationResponse> edit(@RequestBody ExpenseRowRequest r){return ResponseEntity.ok(service.edit(r));}
    @PostMapping("/summary/delete") public ResponseEntity<ExpenseDeleteResponse> delete(@RequestBody ExpenseRowRequest r){return ResponseEntity.ok(service.delete(r));}
    @PostMapping("/details/initialize") public ResponseEntity<ExpenseDetailsInitializeResponse> detailsInitialize(@RequestBody ExpenseDetailsInitializeRequest r){return ResponseEntity.ok(service.initializeDetails(r));}
    @PostMapping("/details/next") public ResponseEntity<ExpenseDetailsNavigationResponse> detailsNext(@RequestBody ExpenseDetailsSaveRequest r){return ResponseEntity.ok(service.detailsNext(r));}
    @PostMapping("/details/previous") public ResponseEntity<ExpenseDetailsNavigationResponse> detailsPrevious(@RequestBody ExpenseDetailsSaveRequest r){return ResponseEntity.ok(service.detailsPrevious(r));}
}
