package com.zest.assignment.dto.product;

public class ProductItemResponse {

    private Long id;
    private Integer quantity;

    public ProductItemResponse(Long id, Integer quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Integer getQuantity() {
        return quantity;
    }
}