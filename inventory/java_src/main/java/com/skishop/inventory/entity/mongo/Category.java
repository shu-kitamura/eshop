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

/**
 * Category entity (MongoDB)
 * Manages the hierarchical structure of product categories
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Document(collection = "categories")
public class Category {

    @Id
    private String id;

    @Indexed
    private String name;

    private String description;

    /**
     * Parent category ID (for hierarchical structure)
     */
    @Indexed
    private String parentId;

    /**
     * Hierarchy level (0: root category)
     */
    @Indexed
    private Integer level;

    /**
     * Category path (e.g., "Sports Equipment/Winter Sports/Ski")
     */
    private String path;

    /**
     * Active status
     */
    @Indexed
    private Boolean active;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Update timestamp
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
        if (level == null) {
            level = 0;
        }
    }

    /**
     * Pre-processing before entity update
     */
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
