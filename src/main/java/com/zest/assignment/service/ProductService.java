package com.zest.assignment.service;

import com.zest.assignment.dto.common.PageResponse;
import com.zest.assignment.dto.product.ProductResponse;
import com.zest.assignment.dto.product.ProductCreateRequest;
import com.zest.assignment.dto.product.ProductUpdateRequest;

public interface ProductService {

    PageResponse<ProductResponse> getAllProducts(int page, int size);

    ProductResponse getProductById(Long id);

    ProductResponse createProduct(ProductCreateRequest request);

    ProductResponse updateProduct(Long id, ProductUpdateRequest request);

    void deleteProduct(Long id);
}