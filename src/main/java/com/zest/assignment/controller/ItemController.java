package com.zest.assignment.controller;

import com.zest.assignment.dto.item.ItemCreateRequest;
import com.zest.assignment.dto.item.ItemResponse;
import com.zest.assignment.dto.product.ProductItemResponse;
import com.zest.assignment.service.ItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.zest.assignment.dto.common.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
@RestController
@RequestMapping("/api/v1/products/{productId}/items")
@Tag(
        name = "Items",
        description = "Product item management APIs"
)
@Validated
@SecurityRequirement(name = "bearerAuth")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(
            summary = "Get items for a product",
            description = "Returns all items associated with a product."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication is required"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping
    public ResponseEntity<PageResponse<ProductItemResponse>> getItems(
            @PathVariable Long productId,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be 0 or greater")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size must not exceed 100")
            int size) {

        PageResponse<ProductItemResponse> response =
                itemService.getItemsByProductId(
                        productId,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Create an item for a product",
            description = "Creates an item for the specified product. ADMIN role is required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid item data"),
            @ApiResponse(responseCode = "401", description = "Authentication is required"),
            @ApiResponse(responseCode = "403", description = "ADMIN role is required"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping
    public ResponseEntity<ItemResponse> createItem(
            @PathVariable Long productId,
            @Valid @RequestBody ItemCreateRequest request) {

        ItemResponse response =
                itemService.createItem(productId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}