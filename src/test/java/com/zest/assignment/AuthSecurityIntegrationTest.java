package com.zest.assignment;

import com.zest.assignment.entity.User;
import com.zest.assignment.enums.Role;
import com.zest.assignment.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRejectUnauthenticatedProductRequest() throws Exception {

        mockMvc.perform(
                        get("/api/v1/products")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        String username = "testuser" + System.currentTimeMillis();

        String requestBody = """
                {
                    "username": "%s",
                    "password": "TestUser@123"
                }
                """.formatted(username);

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        User user = userRepository.findByUsername(username)
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                Role.USER,
                user.getRole()
        );
    }

    @Test
    void shouldLoginSuccessfullyAndReturnTokens() throws Exception {

        String username = "loginuser" + System.currentTimeMillis();

        String registerBody = """
                {
                    "username": "%s",
                    "password": "LoginUser@123"
                }
                """.formatted(username);

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerBody)
                )
                .andExpect(status().isCreated());

        String loginBody = """
                {
                    "username": "%s",
                    "password": "LoginUser@123"
                }
                """.formatted(username);

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void shouldAllowAuthenticatedUserToReadProducts() throws Exception {

        String username = "reader" + System.currentTimeMillis();

        registerUser(username);

        String accessToken = loginAndGetAccessToken(
                username,
                "Reader@123"
        );

        mockMvc.perform(
                        get("/api/v1/products")
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectUserFromCreatingProduct() throws Exception {

        String username = "normaluser" + System.currentTimeMillis();

        registerUser(username);

        String accessToken = loginAndGetAccessToken(
                username,
                "Reader@123"
        );

        String productBody = """
                {
                    "productName": "User Product"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/products")
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productBody)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void shouldAllowAdminToCreateProduct() throws Exception {

        String accessToken = loginAndGetAccessToken(
                "testadmin",
                "TestAdmin@12345"
        );

        String productBody = """
                {
                    "productName": "Admin Product"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/products")
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Admin Product"))
                .andExpect(jsonPath("$.createdBy").value("testadmin"));
    }

    private void registerUser(String username) throws Exception {

        String registerBody = """
                {
                    "username": "%s",
                    "password": "Reader@123"
                }
                """.formatted(username);

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerBody)
                )
                .andExpect(status().isCreated());
    }

    private String loginAndGetAccessToken(
            String username,
            String password
    ) throws Exception {

        String loginBody = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);

        String response = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract accessToken without requiring another JSON library.
        int start = response.indexOf("\"accessToken\":\"") + 15;
        int end = response.indexOf("\"", start);

        return response.substring(start, end);
    }
}