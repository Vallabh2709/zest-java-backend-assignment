package com.zest.assignment.service;

import com.zest.assignment.dto.auth.LoginRequest;
import com.zest.assignment.dto.auth.RegisterRequest;
import com.zest.assignment.dto.auth.TokenResponse;
import com.zest.assignment.entity.RefreshToken;
import com.zest.assignment.entity.User;
import com.zest.assignment.enums.Role;
import com.zest.assignment.exception.ConflictException;
import com.zest.assignment.exception.UnauthorizedException;
import com.zest.assignment.repository.RefreshTokenRepository;
import com.zest.assignment.repository.UserRepository;
import com.zest.assignment.security.JwtService;
import com.zest.assignment.service.impl.AuthServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {

        authService = new AuthServiceImpl(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                7L,
                900000L
        );
    }

    // ---------------------------------------------------------
    // Test 1: Register successfully
    // ---------------------------------------------------------

    @Test
    void shouldRegisterUser() {

        RegisterRequest request =
                new RegisterRequest();

        request.setUsername("john");
        request.setPassword("Password@123");

        when(userRepository.existsByUsername("john"))
                .thenReturn(false);

        when(passwordEncoder.encode("Password@123"))
                .thenReturn("encodedPassword");

        authService.register(request);

        verify(userRepository)
                .existsByUsername("john");

        verify(passwordEncoder)
                .encode("Password@123");

        verify(userRepository)
                .save(any(User.class));
    }

    // ---------------------------------------------------------
    // Test 2: Duplicate username
    // ---------------------------------------------------------

    @Test
    void shouldThrowConflictExceptionWhenUsernameAlreadyExists() {

        RegisterRequest request =
                new RegisterRequest();

        request.setUsername("john");
        request.setPassword("Password@123");

        when(userRepository.existsByUsername("john"))
                .thenReturn(true);

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                "Username already exists",
                exception.getMessage()
        );

        verify(userRepository)
                .existsByUsername("john");

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ---------------------------------------------------------
    // Test 3: Login successfully
    // ---------------------------------------------------------

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request =
                new LoginRequest();

        request.setUsername("admin");
        request.setPassword("Admin@12345");

        User user =
                new User(
                        "admin",
                        "encodedPassword",
                        Role.ADMIN
                );

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername("admin")
                        .password("encodedPassword")
                        .authorities("ROLE_ADMIN")
                        .build();

        RefreshToken refreshToken =
                new RefreshToken(
                        "refresh-token",
                        user,
                        LocalDateTime.now().plusDays(7)
                );

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(userDetails))
                .thenReturn("access-token");

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(refreshToken);

        TokenResponse response =
                authService.login(request);

        assertEquals(
                "access-token",
                response.getAccessToken()
        );

        assertEquals(
                "refresh-token",
                response.getRefreshToken()
        );

        assertEquals(
                "Bearer",
                response.getTokenType()
        );

        assertEquals(
                900,
                response.getExpiresIn()
        );

        verify(authenticationManager)
                .authenticate(any());

        verify(userRepository)
                .findByUsername("admin");

        verify(jwtService)
                .generateToken(userDetails);

        verify(refreshTokenRepository)
                .save(any(RefreshToken.class));
    }

    // ---------------------------------------------------------
    // Test 4: Invalid login credentials
    // ---------------------------------------------------------

    @Test
    void shouldThrowUnauthorizedExceptionWhenLoginFails() {

        LoginRequest request =
                new LoginRequest();

        request.setUsername("admin");
        request.setPassword("WrongPassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(
                        new BadCredentialsException(
                                "Invalid credentials"
                        )
                );

        UnauthorizedException exception =
                assertThrows(
                        UnauthorizedException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid username or password",
                exception.getMessage()
        );

        verify(authenticationManager)
                .authenticate(any());

        verify(userRepository, never())
                .findByUsername(anyString());

        verify(jwtService, never())
                .generateToken(any());

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    // ---------------------------------------------------------
    // Test 5: Refresh token successfully
    // ---------------------------------------------------------

    @Test
    void shouldRefreshTokensSuccessfully() {

        User user =
                new User(
                        "admin",
                        "encodedPassword",
                        Role.ADMIN
                );

        RefreshToken oldRefreshToken =
                new RefreshToken(
                        "old-refresh-token",
                        user,
                        LocalDateTime.now().plusDays(7)
                );

        RefreshToken newRefreshToken =
                new RefreshToken(
                        "new-refresh-token",
                        user,
                        LocalDateTime.now().plusDays(7)
                );

        when(refreshTokenRepository.findByToken(
                "old-refresh-token"))
                .thenReturn(Optional.of(oldRefreshToken));

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(newRefreshToken);

        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("new-access-token");

        TokenResponse response =
                authService.refresh("old-refresh-token");

        assertEquals(
                "new-access-token",
                response.getAccessToken()
        );

        assertEquals(
                "new-refresh-token",
                response.getRefreshToken()
        );

        assertEquals(
                "Bearer",
                response.getTokenType()
        );

        assertEquals(
                900,
                response.getExpiresIn()
        );

        assertTrue(oldRefreshToken.isRevoked());

        verify(refreshTokenRepository)
                .findByToken("old-refresh-token");

        verify(refreshTokenRepository)
                .save(any(RefreshToken.class));

        verify(jwtService)
                .generateToken(any(UserDetails.class));
    }

    // ---------------------------------------------------------
    // Test 6: Invalid refresh token
    // ---------------------------------------------------------

    @Test
    void shouldThrowUnauthorizedExceptionForInvalidRefreshToken() {

        when(refreshTokenRepository.findByToken(
                "invalid-token"))
                .thenReturn(Optional.empty());

        UnauthorizedException exception =
                assertThrows(
                        UnauthorizedException.class,
                        () -> authService.refresh("invalid-token")
                );

        assertEquals(
                "Invalid refresh token",
                exception.getMessage()
        );

        verify(refreshTokenRepository)
                .findByToken("invalid-token");

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verify(jwtService, never())
                .generateToken(any(UserDetails.class));
    }

    // ---------------------------------------------------------
    // Test 7: Logout revokes refresh token
    // ---------------------------------------------------------

    @Test
    void shouldLogoutByRevokingRefreshToken() {

        User user =
                new User(
                        "admin",
                        "encodedPassword",
                        Role.ADMIN
                );

        RefreshToken refreshToken =
                new RefreshToken(
                        "refresh-token",
                        user,
                        LocalDateTime.now().plusDays(7)
                );

        when(refreshTokenRepository.findByToken(
                "refresh-token"))
                .thenReturn(Optional.of(refreshToken));

        Authentication loggedInUser =
                new org.springframework.security.authentication
                        .UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(loggedInUser);

        try {

            authService.logout("refresh-token");

            assertTrue(refreshToken.isRevoked());

            verify(refreshTokenRepository)
                    .findByToken("refresh-token");

        } finally {

            SecurityContextHolder.clearContext();
        }
    }

    // ---------------------------------------------------------
    // Test 8: Reject logout for another user's token
    // ---------------------------------------------------------

    @Test
    void shouldRejectLogoutWhenRefreshTokenBelongsToAnotherUser() {

        User tokenOwner =
                new User(
                        "user1",
                        "encodedPassword",
                        Role.USER
                );

        RefreshToken refreshToken =
                new RefreshToken(
                        "refresh-token",
                        tokenOwner,
                        LocalDateTime.now().plusDays(7)
                );

        when(refreshTokenRepository.findByToken(
                "refresh-token"))
                .thenReturn(Optional.of(refreshToken));

        Authentication loggedInUser =
                new org.springframework.security.authentication
                        .UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(loggedInUser);

        try {

            UnauthorizedException exception =
                    assertThrows(
                            UnauthorizedException.class,
                            () -> authService.logout("refresh-token")
                    );

            assertEquals(
                    "Refresh token does not belong to the authenticated user",
                    exception.getMessage()
            );

            assertFalse(refreshToken.isRevoked());

        } finally {

            SecurityContextHolder.clearContext();
        }
    }
}