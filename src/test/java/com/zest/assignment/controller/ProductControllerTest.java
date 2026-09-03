package com.zest.assignment.controller;

import com.zest.assignment.dto.common.PageResponse;
import com.zest.assignment.dto.product.ProductCreateRequest;
import com.zest.assignment.dto.product.ProductResponse;
import com.zest.assignment.exception.GlobalExceptionHandler;
import com.zest.assignment.exception.ResourceNotFoundException;
import com.zest.assignment.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------------------------------------------------------
    // Test 1: GET all products
    // ---------------------------------------------------------

    @Test
    void shouldGetAllProducts() throws Exception {

        ProductResponse product =
                new ProductResponse(
                        1L,
                        "Laptop",
                        "admin",
                        LocalDateTime.now(),
                        null,
                        null
                );

        PageResponse<ProductResponse> pageResponse =
                new PageResponse<>(
                        List.of(product),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                );

        when(productService.getAllProducts(0, 10))
                .thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/v1/products")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName")
                        .value("Laptop"))
                .andExpect(jsonPath("$.page")
                        .value(0))
                .andExpect(jsonPath("$.size")
                        .value(10));

        verify(productService)
                .getAllProducts(0, 10);
    }

    // ---------------------------------------------------------
    // Test 2: GET product by ID
    // ---------------------------------------------------------

    @Test
    void shouldGetProductById() throws Exception {

        ProductResponse product =
                new ProductResponse(
                        1L,
                        "Laptop",
                        "admin",
                        LocalDateTime.now(),
                        null,
                        null
                );

        when(productService.getProductById(1L))
                .thenReturn(product);

        mockMvc.perform(
                        get("/api/v1/products/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.productName")
                        .value("Laptop"))
                .andExpect(jsonPath("$.createdBy")
                        .value("admin"));

        verify(productService)
                .getProductById(1L);
    }

    // ---------------------------------------------------------
    // Test 3: POST product
    // ---------------------------------------------------------

    @Test
    void shouldCreateProduct() throws Exception {

        ProductResponse product =
                new ProductResponse(
                        1L,
                        "Laptop",
                        "admin",
                        LocalDateTime.now(),
                        null,
                        null
                );

        when(productService.createProduct(
                any(ProductCreateRequest.class)))
                .thenReturn(product);

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "productName": "Laptop"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName")
                        .value("Laptop"))
                .andExpect(jsonPath("$.createdBy")
                        .value("admin"));

        verify(productService)
                .createProduct(
                        any(ProductCreateRequest.class)
                );
    }

    // ---------------------------------------------------------
    // Test 4: Invalid product request
    // ---------------------------------------------------------

    @Test
    void shouldReturnBadRequestForInvalidProduct()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "productName": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(productService, never())
                .createProduct(
                        any(ProductCreateRequest.class)
                );
    }

    // ---------------------------------------------------------
    // Test 5: Product not found
    // ---------------------------------------------------------

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist()
            throws Exception {

        when(productService.getProductById(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Product not found with id: 99"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/products/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Product not found with id: 99"
                        ));

        verify(productService)
                .getProductById(99L);
    }
}