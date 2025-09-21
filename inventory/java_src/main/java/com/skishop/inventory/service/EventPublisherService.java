package com.skishop.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Event Publishing Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish product created event
     */
    public void publishProductCreatedEvent(String productId) {
        try {
            ProductCreatedEvent event = new ProductCreatedEvent(productId);
            kafkaTemplate.send("inventory.product.created", productId, event);
            log.info("Product created event published - Product ID: {}", productId);
        } catch (Exception e) {
            log.error("Failed to publish product created event - Product ID: {}", productId, e);
        }
    }

    /**
     * Publish stock reserved event
     */
    public void publishStockReservedEvent(String productId, Integer quantity) {
        try {
            StockReservedEvent event = new StockReservedEvent(productId, quantity);
            kafkaTemplate.send("inventory.stock.reserved", productId, event);
            log.info("Stock reserved event published - Product ID: {}, Quantity: {}", productId, quantity);
        } catch (Exception e) {
            log.error("Failed to publish stock reserved event - Product ID: {}, Quantity: {}", productId, quantity, e);
        }
    }

    /**
     * Publish stock released event
     */
    public void publishStockReleasedEvent(String productId, Integer quantity) {
        try {
            StockReleasedEvent event = new StockReleasedEvent(productId, quantity);
            kafkaTemplate.send("inventory.stock.released", productId, event);
            log.info("Stock released event published - Product ID: {}, Quantity: {}", productId, quantity);
        } catch (Exception e) {
            log.error("Failed to publish stock released event - Product ID: {}, Quantity: {}", productId, quantity, e);
        }
    }

    /**
     * Publish stock in event
     */
    public void publishStockInEvent(String productId, Integer quantity) {
        try {
            StockInEvent event = new StockInEvent(productId, quantity);
            kafkaTemplate.send("inventory.stock.in", productId, event);
            log.info("Stock in event published - Product ID: {}, Quantity: {}", productId, quantity);
        } catch (Exception e) {
            log.error("Failed to publish stock in event - Product ID: {}, Quantity: {}", productId, quantity, e);
        }
    }

    /**
     * Publish stock out event
     */
    public void publishStockOutEvent(String productId, Integer quantity) {
        try {
            StockOutEvent event = new StockOutEvent(productId, quantity);
            kafkaTemplate.send("inventory.stock.out", productId, event);
            log.info("Stock out event published - Product ID: {}, Quantity: {}", productId, quantity);
        } catch (Exception e) {
            log.error("Failed to publish stock out event - Product ID: {}, Quantity: {}", productId, quantity, e);
        }
    }

    /**
     * Publish inventory status changed event
     */
    public void publishInventoryStatusChangedEvent(String productId, String status) {
        try {
            InventoryStatusChangedEvent event = new InventoryStatusChangedEvent(productId, status);
            kafkaTemplate.send("inventory.status.changed", productId, event);
            log.info("Inventory status changed event published - Product ID: {}, Status: {}", productId, status);
        } catch (Exception e) {
            log.error("Failed to publish inventory status changed event - Product ID: {}, Status: {}", productId, status, e);
        }
    }

    // Event class definitions
    public record ProductCreatedEvent(String productId) {}
    public record StockReservedEvent(String productId, Integer quantity) {}
    public record StockReleasedEvent(String productId, Integer quantity) {}
    public record StockInEvent(String productId, Integer quantity) {}
    public record StockOutEvent(String productId, Integer quantity) {}
    public record InventoryStatusChangedEvent(String productId, String status) {}
}
