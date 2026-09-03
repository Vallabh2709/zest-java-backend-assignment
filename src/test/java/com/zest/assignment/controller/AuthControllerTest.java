package com.zest.assignment.controller;

import com.zest.assignment.dto.auth.LoginRequest;
import com.zest.assignment.dto.auth.RefreshTokenRequest;
import com.zest.assignment.dto.auth.RegisterRequest;
import com.zest.assignment.dto.auth.TokenResponse;
import com.zest.assignment.service.AuthService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private void setUpMockMvc() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .build();
    }


    // ---------------------------------------------------------
    // Test 1: Register
    // ---------------------------------------------------------

    @Test
    void shouldRegisterUser() throws Exception {

        setUpMockMvc();

        doNothing()
                .when(authService)
                .register(any(RegisterRequest.class));

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "john",
                                            "password": "Password@123"
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        verify(authService)
                .register(any(RegisterRequest.class));
    }


    // ---------------------------------------------------------
    // Test 2: Login
    // ---------------------------------------------------------

    @Test
    void shouldLoginSuccessfully() throws Exception {

        setUpMockMvc();

        TokenResponse tokenResponse =
                new TokenResponse(
                        "access-token",
                        "refresh-token",
                        "Bearer",
                        900
                );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(tokenResponse);

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "admin",
                                            "password": "Admin@12345"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("access-token"))
                .andExpect(jsonPath("$.refreshToken")
                        .value("refresh-token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(900));

        verify(authService)
                .login(any(LoginRequest.class));
    }


    // ---------------------------------------------------------
    // Test 3: Refresh token
    // ---------------------------------------------------------

    @Test
    void shouldRefreshToken() throws Exception {

        setUpMockMvc();

        TokenResponse tokenResponse =
                new TokenResponse(
                        "new-access-token",
                        "new-refresh-token",
                        "Bearer",
                        900
                );

        when(authService.refresh("old-refresh-token"))
                .thenReturn(tokenResponse);

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "refreshToken": "old-refresh-token"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken")
                        .value("new-refresh-token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"));

        verify(authService)
                .refresh("old-refresh-token");
    }


    // ---------------------------------------------------------
    // Test 4: Logout
    // ---------------------------------------------------------

    @Test
    void shouldLogoutSuccessfully() throws Exception {

        setUpMockMvc();

        doNothing()
                .when(authService)
                .logout("refresh-token");

        mockMvc.perform(
                        post("/api/v1/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "refreshToken": "refresh-token"
                                        }
                                        """)
                )
                .andExpect(status().isNoContent());

        verify(authService)
                .logout("refresh-token");
    }
}