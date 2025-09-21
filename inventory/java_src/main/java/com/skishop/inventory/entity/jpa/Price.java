package com.skishop.inventory.entity.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Price entity (PostgreSQL)
 * Manages product price information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "prices", indexes = {
    @Index(name = "idx_prices_product_id", columnList = "productId"),
    @Index(name = "idx_prices_active", columnList = "isActive"),
    @Index(name = "idx_prices_sale_dates", columnList = "saleStartDate, saleEndDate")
})
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Product ID (refers to MongoDB Product.id)
     */
    @Column(nullable = false)
    private String productId;

    /**
     * Regular price
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal regularPrice;

    /**
     * Sale price
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal salePrice;

    /**
     * Sale start date/time
     */
    private LocalDateTime saleStartDate;

    /**
     * Sale end date/time
     */
    private LocalDateTime saleEndDate;

    /**
     * Currency code
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currencyCode = "JPY";

    /**
     * Active status
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

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
        if (isActive == null) {
            isActive = true;
        }
        if (currencyCode == null) {
            currencyCode = "JPY";
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
     * Get current effective price
     */
    public BigDecimal getCurrentPrice() {
        LocalDateTime now = LocalDateTime.now();
        if (salePrice != null && 
            saleStartDate != null && 
            saleEndDate != null &&
            !now.isBefore(saleStartDate) && 
            !now.isAfter(saleEndDate)) {
            return salePrice;
        }
        return regularPrice;
    }

    /**
     * Determine if the product is on sale
     */
    public boolean isOnSale() {
        LocalDateTime now = LocalDateTime.now();
        return salePrice != null && 
               saleStartDate != null && 
               saleEndDate != null &&
               !now.isBefore(saleStartDate) && 
               !now.isAfter(saleEndDate);
    }
}
