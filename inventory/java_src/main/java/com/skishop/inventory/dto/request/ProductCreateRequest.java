package com.skishop.inventory.dto.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Product creation request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must be within 50 characters")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must be within 200 characters")
    private String name;

    @Size(max = 1000, message = "Product description must be within 1000 characters")
    private String description;

    @Size(max = 100, message = "Brand name must be within 100 characters")
    private String brand;

    /**
     * Product attributes
     */
    private Map<String, Object> attributes;

    /**
     * Product tags
     */
    private List<String> tags;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    /**
     * Price information
     */
    @NotNull(message = "Price information is required")
    private PriceRequest price;

    /**
     * Inventory information
     */
    @NotNull(message = "Inventory information is required")
    private InventoryRequest inventory;

    /**
     * Price request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceRequest {
        @NotNull(message = "Regular price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Regular price must be greater than 0")
        private BigDecimal regularPrice;

        @DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than 0")
        private BigDecimal salePrice;

        private LocalDateTime saleStartDate;
        private LocalDateTime saleEndDate;

        @Size(max = 3, message = "Currency code must be 3 characters")
        @Builder.Default
        private String currencyCode = "JPY";
    }

    /**
     * Inventory request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryRequest {
        @NotNull(message = "Inventory quantity is required")
        @Min(value = 0, message = "Inventory quantity must be 0 or greater")
        private Integer quantity;

        @NotBlank(message = "Location code is required")
        @Size(max = 20, message = "Location code must be within 20 characters")
        private String locationCode;
    }
}
