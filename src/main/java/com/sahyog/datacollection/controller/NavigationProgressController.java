package com.sahyog.datacollection.controller;

import com.sahyog.datacollection.dto.navigation.NavigationProgressRequest;
import com.sahyog.datacollection.dto.navigation.NavigationProgressResponse;
import com.sahyog.datacollection.service.NavigationProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/data-collection")
public class NavigationProgressController {
    private final NavigationProgressService service;

    public NavigationProgressController(NavigationProgressService service) {
        this.service = service;
    }

    @PostMapping("/navigation-progress")
    public ResponseEntity<NavigationProgressResponse> getProgress(@RequestBody NavigationProgressRequest request) {
        return ResponseEntity.ok(service.getProgress(request));
    }
}
