package com.zest.assignment.service;

import com.zest.assignment.dto.auth.LoginRequest;
import com.zest.assignment.dto.auth.RegisterRequest;
import com.zest.assignment.dto.auth.TokenResponse;

public interface AuthService {

    void register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(String refreshToken);

    void logout(String refreshToken);
}