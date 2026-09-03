package com.zest.assignment.service.impl;

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
import com.zest.assignment.service.AuthService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final long refreshTokenExpirationDays;
    private final long jwtExpirationMs;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthServiceImpl(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            @Value("${app.refresh-token.expiration-days}") long refreshTokenExpirationDays,
            @Value("${app.jwt.expiration-ms}") long jwtExpirationMs) {

        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @Override
    public void register(RegisterRequest request) {

        String username = request.getUsername().trim();

        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists");
        }

        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        /*
         * Public registration always creates a USER.
         *
         * Users cannot register themselves as ADMIN.
         * ADMIN users are provisioned separately by the application.
         */
        User user = new User(
                username,
                encodedPassword,
                Role.USER
        );

        userRepository.save(user);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @Override
    public TokenResponse login(LoginRequest request) {

        String username = request.getUsername().trim();

        Authentication authentication;

        try {

            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    request.getPassword()
                            )
                    );

        } catch (AuthenticationException exception) {

            throw new UnauthorizedException(
                    "Invalid username or password"
            );
        }

        /*
         * Authentication succeeded, so we retrieve the actual
         * application User entity to associate the refresh token
         * with the correct database user.
         */
        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(
                                () -> new UnauthorizedException(
                                        "Invalid username or password"
                                )
                        );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        // Generate short-lived JWT access token.
        String accessToken =
                jwtService.generateToken(userDetails);

        // Generate long-lived refresh token.
        RefreshToken refreshToken =
                createRefreshToken(user);

        return new TokenResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                getAccessTokenExpirationSeconds()
        );
    }

    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    @Override
    public TokenResponse refresh(String refreshTokenValue) {

        if (refreshTokenValue == null ||
                refreshTokenValue.isBlank()) {

            throw new UnauthorizedException(
                    "Refresh token is required"
            );
        }

        /*
         * Find the refresh token in the database.
         */
        RefreshToken oldRefreshToken =
                refreshTokenRepository
                        .findByToken(refreshTokenValue)
                        .orElseThrow(
                                () -> new UnauthorizedException(
                                        "Invalid refresh token"
                                )
                        );

        /*
         * Token must:
         * 1. Not be revoked
         * 2. Not be expired
         */
        if (!oldRefreshToken.isValid()) {

            throw new UnauthorizedException(
                    "Refresh token is expired or revoked"
            );
        }

        User user = oldRefreshToken.getUser();

        /*
         * Refresh token rotation:
         *
         * OLD TOKEN
         *     ↓
         * revoked
         *
         * NEW TOKEN
         *     ↓
         * generated
         *
         * This prevents reuse of the old refresh token.
         */
        oldRefreshToken.revoke();

        RefreshToken newRefreshToken =
                createRefreshToken(user);

        /*
         * Build UserDetails from the database user so that
         * the newly generated JWT contains the correct role.
         */
        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities(
                                "ROLE_" + user.getRole().name()
                        )
                        .build();

        String newAccessToken =
                jwtService.generateToken(userDetails);

        return new TokenResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                "Bearer",
                getAccessTokenExpirationSeconds()
        );
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @Override
    public void logout(String refreshToken) {

        /*
         * Logout with an empty refresh token does not need
         * database processing.
         */
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        RefreshToken token =
                refreshTokenRepository
                        .findByToken(refreshToken)
                        .orElseThrow(
                                () -> new UnauthorizedException(
                                        "Invalid refresh token"
                                )
                        );

        /*
         * The refresh token must belong to the currently
         * authenticated user.
         */
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new UnauthorizedException(
                    "User is not authenticated"
            );
        }

        String authenticatedUsername =
                authentication.getName();

        if (!token.getUser()
                .getUsername()
                .equals(authenticatedUsername)) {

            throw new UnauthorizedException(
                    "Refresh token does not belong to the authenticated user"
            );
        }

        /*
         * We revoke instead of deleting the token so that
         * a previously issued refresh token cannot be reused.
         */
        token.revoke();
    }

    // =========================================================
    // CREATE REFRESH TOKEN
    // =========================================================

    private RefreshToken createRefreshToken(User user) {

        String tokenValue =
                generateSecureToken();

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plusDays(refreshTokenExpirationDays);

        RefreshToken refreshToken =
                new RefreshToken(
                        tokenValue,
                        user,
                        expiresAt
                );

        return refreshTokenRepository.save(refreshToken);
    }

    // =========================================================
    // SECURE RANDOM TOKEN GENERATION
    // =========================================================

    private String generateSecureToken() {

        byte[] randomBytes =
                new byte[64];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    // =========================================================
    // ACCESS TOKEN EXPIRATION
    // =========================================================

    private long getAccessTokenExpirationSeconds() {

        return jwtExpirationMs / 1000;
    }
}