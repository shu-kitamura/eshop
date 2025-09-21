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
 * Inventory entity (PostgreSQL)
 * Manages product inventory quantity and reserved quantity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "inventory", indexes = {
    @Index(name = "idx_inventory_product_id", columnList = "productId"),
    @Index(name = "idx_inventory_location_code", columnList = "locationCode"),
    @Index(name = "idx_inventory_status", columnList = "status")
})
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Product ID (refers to MongoDB Product.id)
     */
    @Column(nullable = false)
    private String productId;

    /**
     * Inventory quantity
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Reserved quantity
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    /**
     * Warehouse/location code
     */
    @Column(nullable = false)
    private String locationCode;

    /**
     * Inventory status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status;

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
        if (reservedQuantity == null) {
            reservedQuantity = 0;
        }
        if (status == null) {
            status = InventoryStatus.IN_STOCK;
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
     * Calculate available inventory quantity
     */
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    /**
     * Inventory status enum (Java 21 modern syntax)
     */
    public enum InventoryStatus {
        IN_STOCK("In Stock"),
        LOW_STOCK("Low Stock"),
        OUT_OF_STOCK("Out of Stock"),
        DISCONTINUED("Discontinued");

        private final String displayName;

        InventoryStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * Determine status based on inventory quantity (uses Java 21 switch expression)
         */
        public static InventoryStatus fromQuantity(int availableQuantity, int lowStockThreshold) {
            if (availableQuantity <= 0) {
                return OUT_OF_STOCK;
            } else if (availableQuantity <= lowStockThreshold) {
                return LOW_STOCK;
            } else {
                return IN_STOCK;
            }
        }
    }
}
