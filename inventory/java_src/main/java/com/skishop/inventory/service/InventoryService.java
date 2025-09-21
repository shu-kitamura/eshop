package com.skishop.inventory.service;

import com.skishop.inventory.entity.jpa.Inventory;
import com.skishop.inventory.repository.jpa.InventoryRepository;
import com.skishop.inventory.exception.ResourceNotFoundException;
import com.skishop.inventory.exception.InsufficientStockException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Inventory Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final EventPublisherService eventPublisherService;

    /**
     * Get inventory information for a product
     */
    @Cacheable(value = "inventory", key = "#productId")
    public Inventory findByProductId(String productId) {
        log.debug("Retrieve inventory information - Product ID: {}", productId);
        return inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory information not found: " + productId));
    }

    /**
     * Get available inventory quantity for a product
     */
    @Cacheable(value = "availableQuantity", key = "#productId")
    public Integer getAvailableQuantity(String productId) {
        log.debug("Retrieve available inventory quantity - Product ID: {}", productId);
        return inventoryRepository.getAvailableQuantityByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory information not found: " + productId));
    }

    /**
     * Batch retrieve inventory status for multiple products (using Java 21 Stream API improvements)
     */
    public Map<String, Inventory> findByProductIds(List<String> productIds) {
        log.debug("Retrieve inventory for multiple products - Product IDs: {}", productIds);
        List<Inventory> inventories = inventoryRepository.findByProductIdIn(productIds);
        
        // Concise toMap() usage in Java 21
        return inventories.stream()
            .collect(Collectors.toMap(Inventory::getProductId, identity -> identity));
    }

    /**
     * Reserve stock
     */
    @Transactional
    @CacheEvict(value = {"inventory", "availableQuantity"}, key = "#productId")
    public void reserveStock(String productId, Integer quantity) {
        log.info("Start stock reservation - Product ID: {}, Quantity: {}", productId, quantity);

        // Check available stock (direct repository call)
        Integer availableQuantity = inventoryRepository.getAvailableQuantityByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory information not found: " + productId));
        if (availableQuantity < quantity) {
            throw new InsufficientStockException(
                String.format("Insufficient stock. Requested: %d, Available: %d", quantity, availableQuantity));
        }

        // Update reserved quantity
        int updated = inventoryRepository.increaseReservedQuantity(productId, quantity);
        if (updated == 0) {
            throw new InsufficientStockException("Failed to reserve stock");
        }

        // Update inventory status
        updateInventoryStatus(productId);

        // Publish event
        eventPublisherService.publishStockReservedEvent(productId, quantity);

        log.info("Stock reservation completed - Product ID: {}, Quantity: {}", productId, quantity);
    }

    /**
     * Release reserved stock
     */
    @Transactional
    @CacheEvict(value = {"inventory", "availableQuantity"}, key = "#productId")
    public void releaseStock(String productId, Integer quantity) {
        log.info("Start releasing reserved stock - Product ID: {}, Quantity: {}", productId, quantity);

        // Decrease reserved quantity
        int updated = inventoryRepository.decreaseReservedQuantity(productId, quantity);
        if (updated == 0) {
            throw new ResourceNotFoundException("Failed to release reserved stock");
        }

        // Update inventory status
        updateInventoryStatus(productId);

        // Publish event
        eventPublisherService.publishStockReleasedEvent(productId, quantity);

        log.info("Reserved stock released - Product ID: {}, Quantity: {}", productId, quantity);
    }

    /**
     * Stock in process (receiving inventory)
     */
    @Transactional
    @CacheEvict(value = {"inventory", "availableQuantity"}, key = "#productId")
    public void stockIn(String productId, Integer quantity) {
        log.info("Start stock in process - Product ID: {}, Quantity: {}", productId, quantity);

        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory information not found: " + productId));
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory.preUpdate();
        inventoryRepository.save(inventory);

        // Update inventory status
        updateInventoryStatus(productId);

        // Publish event
        eventPublisherService.publishStockInEvent(productId, quantity);

        log.info("Stock in process completed - Product ID: {}, Quantity: {}", productId, quantity);
    }

    /**
     * Stock out process (shipping inventory)
     */
    @Transactional
    @CacheEvict(value = {"inventory", "availableQuantity"}, key = "#productId")
    public void stockOut(String productId, Integer quantity) {
        log.info("Start stock out process - Product ID: {}, Quantity: {}", productId, quantity);

        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory information not found: " + productId));
        
        // Subtract from reserved quantity
        if (inventory.getReservedQuantity() >= quantity) {
            inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
            inventory.setQuantity(inventory.getQuantity() - quantity);
        } else {
            throw new InsufficientStockException("Insufficient reserved quantity");
        }

        inventory.preUpdate();
        inventoryRepository.save(inventory);

        // Update inventory status
        updateInventoryStatus(productId);

        // Publish event
        eventPublisherService.publishStockOutEvent(productId, quantity);

        log.info("Stock out process completed - Product ID: {}, Quantity: {}", productId, quantity);
    }

    /**
     * Update inventory status (using Java 21 switch expression)
     */
    private void updateInventoryStatus(String productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory information not found: " + productId));

        int availableQuantity = inventory.getAvailableQuantity();
        int lowStockThreshold = 5; // Can be made configurable
        
        // Use Java 21 switch expression and pattern matching
        Inventory.InventoryStatus newStatus = Inventory.InventoryStatus.fromQuantity(
            availableQuantity, 
            lowStockThreshold
        );

        if (!newStatus.equals(inventory.getStatus())) {
            inventoryRepository.updateStatusByProductId(productId, newStatus);
            eventPublisherService.publishInventoryStatusChangedEvent(productId, newStatus.name());
        }
    }

    /**
     * Get list of items with low stock
     */
    public List<Inventory> findLowStockItems(Integer threshold) {
        log.debug("Retrieve low stock items - Threshold: {}", threshold);
        return inventoryRepository.findLowAvailableStockItems(threshold);
    }
}
