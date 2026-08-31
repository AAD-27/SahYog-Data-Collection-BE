package com.sahyog.datacollection.controller;

import com.sahyog.datacollection.dto.program.ProgramInitializeRequest;
import com.sahyog.datacollection.dto.program.ProgramInitializeResponse;
import com.sahyog.datacollection.dto.program.ProgramNavigationResponse;
import com.sahyog.datacollection.dto.program.ProgramSaveRequest;
import com.sahyog.datacollection.service.ProgramDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/program-details")
public class ProgramDetailsController {
    private final ProgramDetailsService service;

    public ProgramDetailsController(ProgramDetailsService service) {
        this.service = service;
    }

    @PostMapping("/initialize")
    public ResponseEntity<ProgramInitializeResponse> initialize(@RequestBody ProgramInitializeRequest request) {
        return ResponseEntity.ok(service.initialize(request));
    }

    @PostMapping("/next")
    public ResponseEntity<ProgramNavigationResponse> next(@RequestBody ProgramSaveRequest request) {
        return ResponseEntity.ok(service.next(request));
    }

    @PostMapping("/previous")
    public ResponseEntity<ProgramNavigationResponse> previous(@RequestBody ProgramSaveRequest request) {
        return ResponseEntity.ok(service.previous(request));
    }
}
