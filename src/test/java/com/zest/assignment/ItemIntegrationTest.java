package com.zest.assignment.integration;

import com.zest.assignment.entity.Item;
import com.zest.assignment.entity.Product;
import com.zest.assignment.entity.User;
import com.zest.assignment.enums.Role;
import com.zest.assignment.repository.ItemRepository;
import com.zest.assignment.repository.ProductRepository;
import com.zest.assignment.repository.UserRepository;
import com.zest.assignment.dto.item.ItemCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private Long productId;

    @BeforeEach
    void setUp() throws Exception {
        itemRepository.deleteAll();
        productRepository.deleteAll();

        // Create a normal user if it does not already exist
        if (!userRepository.existsByUsername("itemtestuser")) {
            userRepository.save(
                    new User(
                            "itemtestuser",
                            passwordEncoder.encode("Password@123"),
                            Role.USER
                    )
            );
        }

        adminToken = loginAndGetAccessToken("testadmin", "TestAdmin@12345");
        userToken = loginAndGetAccessToken("itemtestuser", "Password@123");

        Product product = productRepository.save(
                new Product("Test Product", "testadmin")
        );

        productId = product.getId();
    }

    @Test
    void shouldCreateItemAsAdmin() throws Exception {

        String request = """
                {
                    "quantity": 10
                }
                """;

        mockMvc.perform(
                        post("/api/v1/products/{productId}/items", productId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.productId", is(productId.intValue())))
                .andExpect(jsonPath("$.quantity", is(10)));
    }

    @Test
    void shouldRejectUserFromCreatingItem() throws Exception {

        String request = """
                {
                    "quantity": 10
                }
                """;

        mockMvc.perform(
                        post("/api/v1/products/{productId}/items", productId)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetItemsByProductId() throws Exception {

        createItem(10);
        createItem(20);

        mockMvc.perform(
                        get("/api/v1/products/{productId}/items", productId)
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].quantity").exists())
                .andExpect(jsonPath("$.content[1].quantity").exists())
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void shouldReturnItemsWithPagination() throws Exception {

        createItem(10);
        createItem(20);
        createItem(30);

        mockMvc.perform(
                        get("/api/v1/products/{productId}/items", productId)
                                .param("page", "0")
                                .param("size", "2")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    void shouldReturnNotFoundForNonExistingProduct() throws Exception {

        mockMvc.perform(
                        get("/api/v1/products/{productId}/items", 999999999L)
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message")
                        .value("Product not found with id: 999999999"));
    }

    @Test
    void shouldRejectInvalidQuantity() throws Exception {

        String request = """
                {
                    "quantity": 0
                }
                """;

        mockMvc.perform(
                        post("/api/v1/products/{productId}/items", productId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }

    @Test
    void shouldRejectDeletingProductWhenItHasItems() throws Exception {

        createItem(10);

        mockMvc.perform(
                        delete("/api/v1/products/{id}", productId)
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")))
                .andExpect(jsonPath("$.message")
                        .value("Product cannot be deleted because it has associated items"));
    }

    private void createItem(int quantity) throws Exception {

        String request = """
                {
                    "quantity": %d
                }
                """.formatted(quantity);

        mockMvc.perform(
                        post("/api/v1/products/{productId}/items", productId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated());
    }

    private String loginAndGetAccessToken(
            String username,
            String password
    ) throws Exception {

        String request = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);

        String response = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractJsonValue(response, "accessToken");
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
}