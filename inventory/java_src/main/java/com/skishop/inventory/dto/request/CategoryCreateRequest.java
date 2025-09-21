package com.skishop.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Category creation request
 */
public record CategoryCreateRequest(
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be within 100 characters")
    String name,
    
    @Size(max = 500, message = "Category description must be within 500 characters")
    String description,
    
    String parentId,
    
    Integer sortOrder,
    
    Boolean isVisible,
    
    String imageUrl
) {
    public String getName() {
        return name;
    }
}
