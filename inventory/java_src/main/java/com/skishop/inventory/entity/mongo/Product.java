package com.skishop.inventory.entity.mongo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Product entity (MongoDB)
 * Manages product basic information, attributes, and category information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;

    private String name;

    private String description;

    private String brand;

    /**
     * Product attributes (JSON format)
     * Example: {"length": "180cm", "width": "100mm", "color": "Red/Black"}
     */
    private Map<String, Object> attributes;

    /**
     * Product tags
     */
    private List<String> tags;

    /**
     * Category ID
     */
    @Indexed
    private String categoryId;

    /**
     * Active status
     */
    @Indexed
    private Boolean active;

    /**
     * Created date/time
     */
    private LocalDateTime createdAt;

    /**
     * Updated date/time
     */
    private LocalDateTime updatedAt;

    /**
     * Created by
     */
    private String createdBy;

    /**
     * Updated by
     */
    private String updatedBy;

    /**
     * Pre-processing before entity creation
     */
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (active == null) {
            active = true;
        }
    }

    /**
     * Pre-processing before entity update
     */
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
