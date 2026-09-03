package com.zest.assignment.service;

import com.zest.assignment.dto.common.PageResponse;
import com.zest.assignment.dto.item.ItemCreateRequest;
import com.zest.assignment.dto.item.ItemResponse;
import com.zest.assignment.dto.product.ProductItemResponse;
import com.zest.assignment.entity.Item;
import com.zest.assignment.entity.Product;
import com.zest.assignment.exception.ResourceNotFoundException;
import com.zest.assignment.repository.ItemRepository;
import com.zest.assignment.repository.ProductRepository;
import com.zest.assignment.service.impl.ItemServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ItemServiceImpl itemService;


    // ---------------------------------------------------------
    // Test 1: Get items with pagination
    // ---------------------------------------------------------

    @Test
    void shouldGetItemsByProductIdWithPagination() {

        Product product =
                new Product("Laptop", "admin");

        Item item1 =
                new Item(product, 10);

        Item item2 =
                new Item(product, 20);

        Page<Item> itemPage =
                new PageImpl<>(
                        List.of(item1, item2),
                        PageRequest.of(0, 2),
                        2
                );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(itemRepository.findByProduct(
                product,
                PageRequest.of(0, 2)))
                .thenReturn(itemPage);

        PageResponse<ProductItemResponse> response =
                itemService.getItemsByProductId(1L, 0, 2);

        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());

        assertTrue(response.isFirst());
        assertTrue(response.isLast());

        assertEquals(
                10,
                response.getContent()
                        .get(0)
                        .getQuantity()
        );

        assertEquals(
                20,
                response.getContent()
                        .get(1)
                        .getQuantity()
        );

        verify(productRepository)
                .findById(1L);

        verify(itemRepository)
                .findByProduct(
                        product,
                        PageRequest.of(0, 2)
                );
    }


    // ---------------------------------------------------------
    // Test 2: Product not found while getting items
    // ---------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenGettingItemsForNonExistingProduct() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> itemService.getItemsByProductId(
                                99L,
                                0,
                                10
                        )
                );

        assertEquals(
                "Product not found with id: 99",
                exception.getMessage()
        );

        verify(productRepository)
                .findById(99L);

        verify(itemRepository, never())
                .findByProduct(
                        any(Product.class),
                        any()
                );
    }


    // ---------------------------------------------------------
    // Test 3: Create item successfully
    // ---------------------------------------------------------

    @Test
    void shouldCreateItem() {

        Product product =
                new Product("Laptop", "admin");

        ItemCreateRequest request =
                new ItemCreateRequest();

        request.setQuantity(25);

        Item savedItem =
                new Item(product, 25);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(itemRepository.save(any(Item.class)))
                .thenReturn(savedItem);

        ItemResponse response =
                itemService.createItem(1L, request);

        assertEquals(
                25,
                response.getQuantity()
        );

        assertEquals(
                product.getId(),
                response.getProductId()
        );

        verify(productRepository)
                .findById(1L);

        verify(itemRepository)
                .save(any(Item.class));
    }


    // ---------------------------------------------------------
    // Test 4: Product not found while creating item
    // ---------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenCreatingItemForNonExistingProduct() {

        ItemCreateRequest request =
                new ItemCreateRequest();

        request.setQuantity(10);

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> itemService.createItem(
                                99L,
                                request
                        )
                );

        assertEquals(
                "Product not found with id: 99",
                exception.getMessage()
        );

        verify(productRepository)
                .findById(99L);

        verify(itemRepository, never())
                .save(any(Item.class));
    }
}