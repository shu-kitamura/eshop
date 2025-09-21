package com.skishop.inventory.entity.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product image entity (PostgreSQL)
 * Manages product image information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "product_images", indexes = {
    @Index(name = "idx_product_images_product_id", columnList = "productId"),
    @Index(name = "idx_product_images_type", columnList = "type"),
    @Index(name = "idx_product_images_sort_order", columnList = "sortOrder")
})
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Product ID (refers to MongoDB Product.id)
     */
    @Column(nullable = false)
    private String productId;

    /**
     * Image URL
     */
    @Column(nullable = false, length = 500)
    private String url;

    /**
     * Thumbnail image URL
     */
    @Column(length = 500)
    private String thumbnailUrl;

    /**
     * Image type
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType type;

    /**
     * Display order
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * Alternative text
     */
    @Column(length = 200)
    private String altText;

    /**
     * Created date/time
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Updated date/time
     */
    @Column(nullable = false)
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
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (type == null) {
            type = ImageType.MAIN;
        }
    }

    /**
     * Pre-processing before entity update
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Image type enum
     */
    public enum ImageType {
        MAIN("Main Image"),
        GALLERY("Gallery Image"),
        DETAIL("Detail Image"),
        THUMBNAIL("Thumbnail Image");

        private final String displayName;

        ImageType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
