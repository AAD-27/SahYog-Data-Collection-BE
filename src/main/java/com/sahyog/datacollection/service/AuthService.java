package com.sahyog.datacollection.service;

import com.sahyog.datacollection.dto.auth.*;

public interface AuthService {
    LoginResponse loginOfficial(LoginRequest request);
    LoginResponse loginCitizen(LoginRequest request);
    CitizenRegistrationResponse registerCitizen(CitizenRegistrationRequest request);
    OfficialRegistrationResponse registerOfficial(OfficialRegistrationRequest request);
    CitizenApplicationContextResponse getCitizenApplicationContext(CitizenApplicationContextRequest request);
    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);
}
