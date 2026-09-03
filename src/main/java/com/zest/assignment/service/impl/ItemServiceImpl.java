package com.zest.assignment.service.impl;

import com.zest.assignment.dto.item.ItemCreateRequest;
import com.zest.assignment.dto.item.ItemResponse;
import com.zest.assignment.dto.product.ProductItemResponse;
import com.zest.assignment.entity.Item;
import com.zest.assignment.entity.Product;
import com.zest.assignment.exception.ResourceNotFoundException;
import com.zest.assignment.repository.ItemRepository;
import com.zest.assignment.repository.ProductRepository;
import com.zest.assignment.service.ItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zest.assignment.dto.common.PageResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.util.List;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ProductRepository productRepository;

    public ItemServiceImpl(
            ItemRepository itemRepository,
            ProductRepository productRepository
    ) {
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductItemResponse> getItemsByProductId(
            Long productId,
            int page,
            int size) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + productId));

        Pageable pageable = PageRequest.of(page, size);

        Page<Item> itemPage =
                itemRepository.findByProduct(product, pageable);

        List<ProductItemResponse> items =
                itemPage.getContent()
                        .stream()
                        .map(item ->
                                new ProductItemResponse(
                                        item.getId(),
                                        item.getQuantity()
                                )
                        )
                        .toList();

        return new PageResponse<>(
                items,
                itemPage.getNumber(),
                itemPage.getSize(),
                itemPage.getTotalElements(),
                itemPage.getTotalPages(),
                itemPage.isFirst(),
                itemPage.isLast()
        );
    }

    @Override
    public ItemResponse createItem(
            Long productId,
            ItemCreateRequest request
    ) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + productId
                        )
                );

        Item item = new Item(
                product,
                request.getQuantity()
        );

        Item savedItem = itemRepository.save(item);

        return new ItemResponse(
                savedItem.getId(),
                product.getId(),
                savedItem.getQuantity()
        );
    }
}