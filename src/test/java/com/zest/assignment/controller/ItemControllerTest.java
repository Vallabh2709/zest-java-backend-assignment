package com.zest.assignment.controller;

import com.zest.assignment.dto.common.PageResponse;
import com.zest.assignment.dto.item.ItemCreateRequest;
import com.zest.assignment.dto.item.ItemResponse;
import com.zest.assignment.dto.product.ProductItemResponse;
import com.zest.assignment.service.ItemService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private MockMvc mockMvc;

    private void setUpMockMvc() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .build();
    }


    // ---------------------------------------------------------
    // Test 1: GET items with pagination
    // ---------------------------------------------------------

    @Test
    void shouldGetItemsByProductId() throws Exception {

        setUpMockMvc();

        ProductItemResponse item =
                new ProductItemResponse(
                        1L,
                        10
                );

        PageResponse<ProductItemResponse> pageResponse =
                new PageResponse<>(
                        List.of(item),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                );

        when(itemService.getItemsByProductId(1L, 0, 10))
                .thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/v1/products/1/items")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id")
                        .value(1))
                .andExpect(jsonPath("$.content[0].quantity")
                        .value(10))
                .andExpect(jsonPath("$.page")
                        .value(0))
                .andExpect(jsonPath("$.size")
                        .value(10));

        verify(itemService)
                .getItemsByProductId(1L, 0, 10);
    }


    // ---------------------------------------------------------
    // Test 2: Create item
    // ---------------------------------------------------------

    @Test
    void shouldCreateItem() throws Exception {

        setUpMockMvc();

        ItemResponse item =
                new ItemResponse(
                        1L,
                        1L,
                        25
                );

        when(itemService.createItem(
                eq(1L),
                any(ItemCreateRequest.class)))
                .thenReturn(item);

        mockMvc.perform(
                        post("/api/v1/products/1/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "quantity": 25
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.productId")
                        .value(1))
                .andExpect(jsonPath("$.quantity")
                        .value(25));

        verify(itemService)
                .createItem(
                        eq(1L),
                        any(ItemCreateRequest.class)
                );
    }


    // ---------------------------------------------------------
    // Test 3: Invalid quantity
    // ---------------------------------------------------------

    @Test
    void shouldReturnBadRequestForInvalidQuantity()
            throws Exception {

        setUpMockMvc();

        mockMvc.perform(
                        post("/api/v1/products/1/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "quantity": 0
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(itemService, never())
                .createItem(
                        eq(1L),
                        any(ItemCreateRequest.class)
                );
    }
}