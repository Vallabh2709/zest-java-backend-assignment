package com.zest.assignment;

import com.zest.assignment.entity.Product;
import com.zest.assignment.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldCreateProduct() throws Exception {

        String accessToken = loginAsAdmin();

        String productName = "Integration Product " + System.currentTimeMillis();

        String requestBody = """
                {
                    "productName": "%s"
                }
                """.formatted(productName);

        mockMvc.perform(
                        post("/api/v1/products")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.productName").value(productName))
                .andExpect(jsonPath("$.createdBy").value("testadmin"))
                .andExpect(jsonPath("$.createdOn").exists());
    }

    @Test
    void shouldGetAllProductsWithPagination() throws Exception {

        String accessToken = loginAsAdmin();

        Product product1 = productRepository.save(
                new Product(
                        "Pagination Product 1 " + System.currentTimeMillis(),
                        "testadmin"
                )
        );

        Product product2 = productRepository.save(
                new Product(
                        "Pagination Product 2 " + System.currentTimeMillis(),
                        "testadmin"
                )
        );

        mockMvc.perform(
                        get("/api/v1/products")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());

        // Keep references so the test explicitly creates real database records.
        assertEquals("testadmin", product1.getCreatedBy());
        assertEquals("testadmin", product2.getCreatedBy());
    }

    @Test
    void shouldGetProductById() throws Exception {

        String accessToken = loginAsAdmin();

        Product product = productRepository.save(
                new Product(
                        "Get By ID Product " + System.currentTimeMillis(),
                        "testadmin"
                )
        );

        mockMvc.perform(
                        get("/api/v1/products/" + product.getId())
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.productName").value(product.getProductName()))
                .andExpect(jsonPath("$.createdBy").value("testadmin"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {

        String accessToken = loginAsAdmin();

        Product product = productRepository.save(
                new Product(
                        "Original Product " + System.currentTimeMillis(),
                        "testadmin"
                )
        );

        String requestBody = """
                {
                    "productName": "Updated Integration Product"
                }
                """;

        mockMvc.perform(
                        put("/api/v1/products/" + product.getId())
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.productName")
                        .value("Updated Integration Product"))
                .andExpect(jsonPath("$.modifiedBy").value("testadmin"))
                .andExpect(jsonPath("$.modifiedOn").exists());

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertEquals(
                "Updated Integration Product",
                updatedProduct.getProductName()
        );
        assertEquals(
                "testadmin",
                updatedProduct.getModifiedBy()
        );
    }

    @Test
    void shouldDeleteProduct() throws Exception {

        String accessToken = loginAsAdmin();

        Product product = productRepository.save(
                new Product(
                        "Delete Product " + System.currentTimeMillis(),
                        "testadmin"
                )
        );

        Long productId = product.getId();

        mockMvc.perform(
                        delete("/api/v1/products/" + productId)
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isNoContent());

        // Verify the record was actually removed from H2.
        assertEquals(false, productRepository.existsById(productId));
    }

    @Test
    void shouldReturnNotFoundForNonExistingProduct() throws Exception {

        String accessToken = loginAsAdmin();

        mockMvc.perform(
                        get("/api/v1/products/999999999")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Product not found with id: 999999999"));
    }

    @Test
    void shouldRejectInvalidProductRequest() throws Exception {

        String accessToken = loginAsAdmin();

        String invalidRequest = """
                {
                    "productName": ""
                }
                """;

        mockMvc.perform(
                        post("/api/v1/products")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    private String loginAsAdmin() throws Exception {

        String loginBody = """
                {
                    "username": "testadmin",
                    "password": "TestAdmin@12345"
                }
                """;

        String response = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int start = response.indexOf("\"accessToken\":\"") + 15;
        int end = response.indexOf("\"", start);

        return response.substring(start, end);
    }
}