package com.skishop.inventory.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product image DTO (using Java 21 record)
 */
public record ProductImageDTO(
    UUID id,
    
    @NotBlank(message = "Product ID is required")
    String productId,
    
    @NotBlank(message = "Image URL is required")
    @Size(max = 500, message = "Image URL must be within 500 characters")
    String url,
    
    @Size(max = 500, message = "Thumbnail image URL must be within 500 characters")
    String thumbnailUrl,
    
    @NotNull(message = "Image type is required")
    String type,
    
    @NotNull(message = "Display order is required")
    @Min(value = 0, message = "Display order must be 0 or greater")
    Integer sortOrder,
    
    @Size(max = 200, message = "Alternative text must be within 200 characters")
    String altText,
    
    /**
     * Created date time
     */
    LocalDateTime createdAt,
    
    /**
     * Updated date time
     */
    LocalDateTime updatedAt
) {
    /**
     * Check if this is a main image
     */
    public boolean isMainImage() {
        return "MAIN".equalsIgnoreCase(type);
    }
    
    /**
     * Check if thumbnail image is available
     */
    public boolean hasThumbnail() {
        return thumbnailUrl != null && !thumbnailUrl.isBlank();
    }
}
