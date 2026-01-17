package com.healthfamily.service;

import com.healthfamily.web.dto.LoginRequest;
import com.healthfamily.web.dto.RegisterRequest;
import com.healthfamily.web.dto.TokenResponse;


public interface AuthService {

    TokenResponse register(RegisterRequest request);

    TokenResponse registerDoctor(RegisterRequest request);

    TokenResponse registerAdmin(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(String token);
    

}

