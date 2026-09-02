package com.sahyog.datacollection.controller;
import com.sahyog.datacollection.dto.eligibility.*;
import com.sahyog.datacollection.service.EligibilitySnapshotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/eligibility")
public class EligibilitySnapshotController {
    private final EligibilitySnapshotService service;
    public EligibilitySnapshotController(EligibilitySnapshotService service) { this.service = service; }
    @PostMapping("/data-snapshot")
    public ResponseEntity<EligibilitySnapshotResponse> snapshot(@RequestBody EligibilitySnapshotRequest request) {
        return ResponseEntity.ok(service.create(request));
    }
}
