package com.sahyog.datacollection.controller;

import com.sahyog.datacollection.dto.auth.*;
import com.sahyog.datacollection.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/government-official/login")
    public LoginResponse officialLogin(@Valid @RequestBody LoginRequest request) {
        return service.loginOfficial(request);
    }

    @PostMapping("/citizen/login")
    public LoginResponse citizenLogin(@Valid @RequestBody LoginRequest request) {
        return service.loginCitizen(request);
    }

    @PostMapping("/citizen/register")
    public ResponseEntity<CitizenRegistrationResponse> register(@Valid @RequestBody CitizenRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registerCitizen(request));
    }

    @PostMapping("/government-official/register")
    public ResponseEntity<OfficialRegistrationResponse> registerOfficial(
            @Valid @RequestBody OfficialRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registerOfficial(request));
    }

    @PostMapping("/citizen/application-context")
    public CitizenApplicationContextResponse citizenApplicationContext(
            @Valid @RequestBody CitizenApplicationContextRequest request) {
        return service.getCitizenApplicationContext(request);
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return service.forgotPassword(request);
    }
}
