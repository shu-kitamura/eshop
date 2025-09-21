package com.skishop.inventory.repository.jpa;

import com.skishop.inventory.entity.jpa.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inventory repository (PostgreSQL)
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    /**
     * Find inventory by product ID
     */
    Optional<Inventory> findByProductId(String productId);

    /**
     * Find inventory by product ID and location
     */
    Optional<Inventory> findByProductIdAndLocationCode(String productId, String locationCode);

    /**
     * Find inventory by multiple product IDs
     */
    List<Inventory> findByProductIdIn(List<String> productIds);

    /**
     * Find inventory by location code
     */
    Page<Inventory> findByLocationCode(String locationCode, Pageable pageable);

    /**
     * Find inventory by status
     */
    Page<Inventory> findByStatus(Inventory.InventoryStatus status, Pageable pageable);

    /**
     * Find items with inventory quantity below specified threshold
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= :threshold")
    List<Inventory> findLowStockItems(@Param("threshold") Integer threshold);

    /**
     * Find items with available inventory quantity below specified threshold
     */
    @Query("SELECT i FROM Inventory i WHERE (i.quantity - i.reservedQuantity) <= :threshold")
    List<Inventory> findLowAvailableStockItems(@Param("threshold") Integer threshold);

    /**
     * Update inventory quantity
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = :quantity, i.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE i.productId = :productId")
    int updateQuantityByProductId(@Param("productId") String productId, 
                                  @Param("quantity") Integer quantity);

    /**
     * Increase reserved quantity
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity + :amount, " +
           "i.updatedAt = CURRENT_TIMESTAMP WHERE i.productId = :productId AND " +
           "(i.quantity - i.reservedQuantity) >= :amount")
    int increaseReservedQuantity(@Param("productId") String productId, 
                                @Param("amount") Integer amount);

    /**
     * Decrease reserved quantity
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :amount, " +
           "i.updatedAt = CURRENT_TIMESTAMP WHERE i.productId = :productId AND " +
           "i.reservedQuantity >= :amount")
    int decreaseReservedQuantity(@Param("productId") String productId, 
                                @Param("amount") Integer amount);

    /**
     * Update inventory status
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.status = :status, i.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE i.productId = :productId")
    int updateStatusByProductId(@Param("productId") String productId, 
                               @Param("status") Inventory.InventoryStatus status);

    /**
     * Get available inventory quantity for a product
     */
    @Query("SELECT (i.quantity - i.reservedQuantity) FROM Inventory i WHERE i.productId = :productId")
    Optional<Integer> getAvailableQuantityByProductId(@Param("productId") String productId);

    /**
     * Check if inventory exists
     */
    boolean existsByProductId(String productId);

    /**
     * Batch retrieve inventory status for multiple products
     */
    @Query("SELECT i.productId, i.quantity, i.reservedQuantity, i.status FROM Inventory i " +
           "WHERE i.productId IN :productIds")
    List<Object[]> findInventoryStatusByProductIds(@Param("productIds") List<String> productIds);
}
