package com.skishop.inventory.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Category DTO (using Java 21 record)
 */
public record CategoryDTO(
    String id,
    
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be within 100 characters")
    String name,
    
    @Size(max = 500, message = "Category description must be within 500 characters")
    String description,
    
    /**
     * Parent category ID
     */
    String parentId,
    
    /**
     * Parent category information
     */
    CategoryDTO parent,
    
    /**
     * Child category list
     */
    List<CategoryDTO> children,
    
    /**
     * Hierarchy level
     */
    Integer level,
    
    /**
     * Category path
     */
    String path,
    
    /**
     * Active status
     */
    Boolean active,
    
    /**
     * Created date time
     */
    LocalDateTime createdAt,
    
    /**
     * Updated date time
     */
    LocalDateTime updatedAt,
    
    /**
     * Product count (number of products belonging to this category)
     */
    Long productCount
) {
    /**
     * Check if this is a root category
     */
    public boolean isRoot() {
        return parentId == null || parentId.isBlank();
    }
    
    /**
     * Check if this category has child categories
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
    
    /**
     * Check if this category is active and available
     */
    public boolean isAvailable() {
        return Boolean.TRUE.equals(active);
    }
}
