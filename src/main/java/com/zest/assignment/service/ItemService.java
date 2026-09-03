package com.zest.assignment.service;

import com.zest.assignment.dto.common.PageResponse;
import com.zest.assignment.dto.item.ItemCreateRequest;
import com.zest.assignment.dto.item.ItemResponse;
import com.zest.assignment.dto.product.ProductItemResponse;

import java.util.List;

public interface ItemService {

    PageResponse<ProductItemResponse> getItemsByProductId(
            Long productId,
            int page,
            int size
    );

    ItemResponse createItem(Long productId, ItemCreateRequest request);
}