package com.skishop.inventory.repository.jpa;

import com.skishop.inventory.entity.jpa.Price;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Price repository (PostgreSQL)
 */
@Repository
public interface PriceRepository extends JpaRepository<Price, UUID> {

    /**
     * Find active price by product ID
     */
    Optional<Price> findByProductIdAndIsActiveTrue(String productId);

    /**
     * Find price history by product ID
     */
    Page<Price> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);

    /**
     * Find active prices by multiple product IDs
     */
    List<Price> findByProductIdInAndIsActiveTrue(List<String> productIds);

    /**
     * Find product prices currently on sale
     */
    @Query("SELECT p FROM Price p WHERE p.isActive = true AND p.salePrice IS NOT NULL AND " +
           "p.saleStartDate <= :now AND p.saleEndDate >= :now")
    List<Price> findActiveSalePrices(@Param("now") LocalDateTime now);

    /**
     * Find products by price range
     */
    @Query("SELECT p FROM Price p WHERE p.isActive = true AND " +
           "((p.salePrice IS NOT NULL AND p.saleStartDate <= :now AND p.saleEndDate >= :now AND " +
           "p.salePrice BETWEEN :minPrice AND :maxPrice) OR " +
           "(p.salePrice IS NULL OR p.saleStartDate > :now OR p.saleEndDate < :now) AND " +
           "p.regularPrice BETWEEN :minPrice AND :maxPrice)")
    Page<Price> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                @Param("maxPrice") BigDecimal maxPrice,
                                @Param("now") LocalDateTime now,
                                Pageable pageable);

    /**
     * Find prices by currency code
     */
    Page<Price> findByCurrencyCodeAndIsActiveTrue(String currencyCode, Pageable pageable);

    /**
     * Find prices with expired sale periods
     */
    @Query("SELECT p FROM Price p WHERE p.isActive = true AND p.saleEndDate < :now")
    List<Price> findExpiredSalePrices(@Param("now") LocalDateTime now);

    /**
     * Get current effective price for a product
     */
    @Query("SELECT CASE " +
           "WHEN p.salePrice IS NOT NULL AND p.saleStartDate <= :now AND p.saleEndDate >= :now " +
           "THEN p.salePrice " +
           "ELSE p.regularPrice END " +
           "FROM Price p WHERE p.productId = :productId AND p.isActive = true")
    Optional<BigDecimal> getCurrentPriceByProductId(@Param("productId") String productId, 
                                                   @Param("now") LocalDateTime now);

    /**
     * Check if product is currently on sale
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM Price p WHERE p.productId = :productId AND p.isActive = true AND " +
           "p.salePrice IS NOT NULL AND p.saleStartDate <= :now AND p.saleEndDate >= :now")
    boolean isProductOnSale(@Param("productId") String productId, @Param("now") LocalDateTime now);

    /**
     * Check if product price exists
     */
    boolean existsByProductIdAndIsActiveTrue(String productId);

    /**
     * Batch retrieve current prices for multiple products
     */
    @Query("SELECT p.productId, " +
           "CASE WHEN p.salePrice IS NOT NULL AND p.saleStartDate <= :now AND p.saleEndDate >= :now " +
           "THEN p.salePrice ELSE p.regularPrice END as currentPrice, " +
           "CASE WHEN p.salePrice IS NOT NULL AND p.saleStartDate <= :now AND p.saleEndDate >= :now " +
           "THEN true ELSE false END as onSale " +
           "FROM Price p WHERE p.productId IN :productIds AND p.isActive = true")
    List<Object[]> getCurrentPricesByProductIds(@Param("productIds") List<String> productIds,
                                               @Param("now") LocalDateTime now);
}
