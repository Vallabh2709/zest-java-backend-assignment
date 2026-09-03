package com.zest.assignment.integration;

import com.zest.assignment.entity.RefreshToken;
import com.zest.assignment.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RefreshTokenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
    }

    @Test
    void shouldRotateRefreshTokenSuccessfully() throws Exception {

        String loginRequest = """
                {
                    "username": "testadmin",
                    "password": "TestAdmin@12345"
                }
                """;

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyString())))
                .andExpect(jsonPath("$.refreshToken", not(emptyString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String oldRefreshToken =
                extractJsonValue(loginResponse, "refreshToken");

        String refreshRequest = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(oldRefreshToken);

        String refreshResponse = mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyString())))
                .andExpect(jsonPath("$.refreshToken", not(emptyString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String newRefreshToken =
                extractJsonValue(refreshResponse, "refreshToken");

        // The rotated refresh token must be different.
        org.junit.jupiter.api.Assertions.assertNotEquals(
                oldRefreshToken,
                newRefreshToken
        );

        // The old refresh token must no longer be usable.
        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshRequest)
                )
                .andExpect(status().isUnauthorized());

        // The new refresh token must still work.
        String newRefreshRequest = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(newRefreshToken);

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(newRefreshRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyString())))
                .andExpect(jsonPath("$.refreshToken", not(emptyString())));
    }

    @Test
    void shouldRejectInvalidRefreshToken() throws Exception {

        String request = """
                {
                    "refreshToken": "invalid-refresh-token"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void shouldLogoutAndRevokeRefreshToken() throws Exception {

        String loginRequest = """
                {
                    "username": "testadmin",
                    "password": "TestAdmin@12345"
                }
                """;

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken =
                extractJsonValue(loginResponse, "accessToken");

        String refreshToken =
                extractJsonValue(loginResponse, "refreshToken");

        String logoutRequest = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        mockMvc.perform(
                        post("/api/v1/auth/logout")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(logoutRequest)
                )
                .andExpect(status().isNoContent());

        // The refresh token must now be unusable.
        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(logoutRequest)
                )
                .andExpect(status().isUnauthorized());
    }

    private String extractJsonValue(String json, String field) {

        String search = "\"" + field + "\":\"";

        int start = json.indexOf(search);

        if (start == -1) {
            throw new IllegalStateException(
                    "Field '" + field + "' not found in response: " + json
            );
        }

        start += search.length();

        int end = json.indexOf("\"", start);

        return json.substring(start, end);
    }

    @Test
    void shouldRejectLogoutWhenRefreshTokenBelongsToAnotherUser() throws Exception {

        String loginRequest = """
            {
                "username": "testadmin",
                "password": "TestAdmin@12345"
            }
            """;

        String adminLoginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String adminAccessToken =
                extractJsonValue(adminLoginResponse, "accessToken");

        String userLoginRequest = """
            {
                "username": "itemtestuser",
                "password": "Password@123"
            }
            """;

        String userLoginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userLoginRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userRefreshToken =
                extractJsonValue(userLoginResponse, "refreshToken");

        String logoutRequest = """
            {
                "refreshToken": "%s"
            }
            """.formatted(userRefreshToken);

        mockMvc.perform(
                        post("/api/v1/auth/logout")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminAccessToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(logoutRequest)
                )
                .andExpect(status().isUnauthorized());
    }
}