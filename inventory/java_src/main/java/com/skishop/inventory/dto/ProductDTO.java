package com.skishop.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Product response DTO
 * 
 * <p>Immutable data class using Java 21 record feature</p>
 * 
 * @param id Product ID
 * @param sku SKU
 * @param name Product name
 * @param description Product description
 * @param brand Brand name
 * @param attributes Product attributes
 * @param tags Product tags
 * @param category Category information
 * @param price Price information
 * @param inventory Inventory information
 * @param images Product images
 * @param imageUrl Main image URL
 * @param active Active status
 * @param createdAt Created date time
 * @param updatedAt Updated date time
 * 
 * @since 1.0.0
 */
public record ProductDTO(
    String id,
    
    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must be within 50 characters")
    String sku,
    
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must be within 200 characters")
    String name,
    
    @Size(max = 1000, message = "Product description must be within 1000 characters")
    String description,
    
    @Size(max = 100, message = "Brand name must be within 100 characters")
    String brand,
    
    Map<String, Object> attributes,
    List<String> tags,
    CategoryDTO category,
    PriceInfoDTO price,
    InventoryInfoDTO inventory,
    List<ProductImageDTO> images,
    String imageUrl,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
    /**
     * Filter only active products
     * 
     * @return true if active
     */
    public boolean isActive() {
        return active != null && active;
    }
    
    /**
     * Check if the product is on sale
     * 
     * @return true if on sale
     */
    public boolean isOnSale() {
        return price != null && price.onSale() != null && price.onSale();
    }
    
    /**
     * Check if the product has stock
     * 
     * @return true if stock is available
     */
    public boolean hasStock() {
        return inventory != null && 
               inventory.availableQuantity() != null && 
               inventory.availableQuantity() > 0;
    }
    
    /**
     * Price information DTO
     * 
     * @param regularPrice Regular price
     * @param salePrice Sale price
     * @param currentPrice Current price
     * @param currencyCode Currency code
     * @param onSale On sale flag
     * @param saleStartDate Sale start date
     * @param saleEndDate Sale end date
     */
    public record PriceInfoDTO(
        BigDecimal regularPrice,
        BigDecimal salePrice,
        BigDecimal currentPrice,
        String currencyCode,
        Boolean onSale,
        LocalDateTime saleStartDate,
        LocalDateTime saleEndDate
    ) {
        
        /**
         * Check if it's within a valid sale period
         * 
         * @return true if within sale period
         */
        public boolean isValidSalePeriod() {
            if (!Boolean.TRUE.equals(onSale)) {
                return false;
            }
            
            var now = LocalDateTime.now();
            var afterStart = saleStartDate == null || !now.isBefore(saleStartDate);
            var beforeEnd = saleEndDate == null || !now.isAfter(saleEndDate);
            
            return afterStart && beforeEnd;
        }
    }
    
    /**
     * Inventory information DTO
     * 
     * @param status Status
     * @param quantity Total quantity
     * @param availableQuantity Available quantity
     * @param locationCode Location code
     */
    public record InventoryInfoDTO(
        String status,
        Integer quantity,
        Integer availableQuantity,
        String locationCode
    ) {
        
        /**
         * Determine inventory status
         * 
         * @return Inventory status string
         */
        public String getStockStatus() {
            var qty = availableQuantity == null ? 0 : availableQuantity;
            return switch (qty) {
                case 0 -> "Out of Stock";
                case 1, 2, 3, 4, 5 -> "Low Stock";
                default -> qty > 10 ? "In Stock" : "Limited Stock";
            };
        }
    }
}
