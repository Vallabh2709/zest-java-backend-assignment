package com.zest.assignment.service.impl;

import com.zest.assignment.dto.common.PageResponse;
import com.zest.assignment.dto.product.ProductCreateRequest;
import com.zest.assignment.dto.product.ProductResponse;
import com.zest.assignment.dto.product.ProductUpdateRequest;
import com.zest.assignment.entity.Product;
import com.zest.assignment.exception.ConflictException;
import com.zest.assignment.exception.ResourceNotFoundException;
import com.zest.assignment.exception.UnauthorizedException;
import com.zest.assignment.repository.ItemRepository;
import com.zest.assignment.repository.ProductRepository;
import com.zest.assignment.service.ProductService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;

    public ProductServiceImpl(
            ProductRepository productRepository,
            ItemRepository itemRepository) {

        this.productRepository = productRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Product> productPage = productRepository.findAll(pageable);

        List<ProductResponse> products = productPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new PageResponse<>(
                products,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isFirst(),
                productPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id));

        return mapToResponse(product);
    }

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {

        String productName = request.getProductName().trim();

        Product product = new Product(
                productName,
                getCurrentUsername()
        );

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    @Override
    public ProductResponse updateProduct(
            Long id,
            ProductUpdateRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id));

        product.updateProductName(
                request.getProductName().trim()
        );

        product.markModifiedBy(
                getCurrentUsername()
        );

        Product updatedProduct = productRepository.saveAndFlush(product);

        return mapToResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id));

        if (itemRepository.existsByProduct(product)) {

            throw new ConflictException(
                    "Product cannot be deleted because it has associated items"
            );
        }

        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCreatedBy(),
                product.getCreatedOn(),
                product.getModifiedBy(),
                product.getModifiedOn()
        );
    }

    private String getCurrentUsername() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new UnauthorizedException(
                    "Authenticated user not found"
            );
        }

        return authentication.getName();
    }
}