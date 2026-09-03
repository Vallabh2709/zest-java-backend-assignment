package com.zest.assignment.service;

import com.zest.assignment.dto.common.PageResponse;
import com.zest.assignment.dto.product.ProductCreateRequest;
import com.zest.assignment.dto.product.ProductResponse;
import com.zest.assignment.dto.product.ProductUpdateRequest;
import com.zest.assignment.entity.Item;
import com.zest.assignment.entity.Product;
import com.zest.assignment.exception.ConflictException;
import com.zest.assignment.exception.ResourceNotFoundException;
import com.zest.assignment.repository.ItemRepository;
import com.zest.assignment.repository.ProductRepository;
import com.zest.assignment.service.impl.ProductServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of()
                )
        );
    }

    @Test
    void shouldGetProductById() {

        Product product =
                new Product("Laptop", "admin");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductResponse response =
                productService.getProductById(1L);

        assertEquals("Laptop", response.getProductName());
        assertEquals("admin", response.getCreatedBy());

        verify(productRepository)
                .findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> productService.getProductById(99L)
                );

        assertEquals(
                "Product not found with id: 99",
                exception.getMessage()
        );

        verify(productRepository)
                .findById(99L);
    }

    @Test
    void shouldGetAllProductsWithPagination() {

        Product product1 =
                new Product("Laptop", "admin");

        Product product2 =
                new Product("Mobile", "admin");

        Page<Product> productPage =
                new PageImpl<>(
                        List.of(product1, product2),
                        PageRequest.of(0, 2),
                        2
                );

        when(productRepository.findAll(PageRequest.of(0, 2)))
                .thenReturn(productPage);

        PageResponse<ProductResponse> response =
                productService.getAllProducts(0, 2);

        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());

        verify(productRepository)
                .findAll(PageRequest.of(0, 2));
    }

    @Test
    void shouldCreateProduct() {

        ProductCreateRequest request =
                new ProductCreateRequest();

        request.setProductName("Keyboard");

        Product savedProduct =
                new Product("Keyboard", "admin");

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        ProductResponse response =
                productService.createProduct(request);

        assertEquals("Keyboard", response.getProductName());
        assertEquals("admin", response.getCreatedBy());

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldUpdateProduct() {

        Product product =
                new Product("Old Laptop", "admin");

        ProductUpdateRequest request =
                new ProductUpdateRequest();

        request.setProductName("New Laptop");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.saveAndFlush(product))
                .thenReturn(product);

        ProductResponse response =
                productService.updateProduct(1L, request);

        assertEquals("New Laptop", response.getProductName());
        assertEquals("admin", response.getModifiedBy());

        verify(productRepository).findById(1L);
        verify(productRepository).saveAndFlush(product);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingProduct() {

        ProductUpdateRequest request =
                new ProductUpdateRequest();

        request.setProductName("New Laptop");

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> productService.updateProduct(99L, request)
                );

        assertEquals(
                "Product not found with id: 99",
                exception.getMessage()
        );

        verify(productRepository)
                .findById(99L);

        verify(productRepository, never())
                .saveAndFlush(any(Product.class));
    }

    @Test
    void shouldDeleteProduct() {

        Product product =
                new Product("Keyboard", "admin");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(itemRepository.existsByProduct(product))
                .thenReturn(false);

        productService.deleteProduct(1L);

        verify(productRepository)
                .findById(1L);

        verify(itemRepository)
                .existsByProduct(product);

        verify(productRepository)
                .delete(product);
    }

    @Test
    void shouldThrowConflictExceptionWhenDeletingProductWithItems() {

        Product product =
                new Product("Laptop", "admin");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(itemRepository.existsByProduct(product))
                .thenReturn(true);

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> productService.deleteProduct(1L)
                );

        assertEquals(
                "Product cannot be deleted because it has associated items",
                exception.getMessage()
        );

        verify(productRepository)
                .findById(1L);

        verify(itemRepository)
                .existsByProduct(product);

        verify(productRepository, never())
                .delete(any(Product.class));
    }
}