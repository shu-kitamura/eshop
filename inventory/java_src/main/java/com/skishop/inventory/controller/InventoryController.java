package com.skishop.inventory.controller;

import com.skishop.inventory.entity.jpa.Inventory;
import com.skishop.inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * Inventory API Controller
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Validated
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Get inventory information for a product
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventory(@PathVariable String productId) {
        log.info("Inventory information request - Product ID: {}", productId);
        
        Inventory inventory = inventoryService.findByProductId(productId);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Get inventory status for a product
     */
    @GetMapping("/status/{productId}")
    public ResponseEntity<InventoryStatusResponse> getInventoryStatus(@PathVariable String productId) {
        log.info("Inventory status request - Product ID: {}", productId);
        
        Inventory inventory = inventoryService.findByProductId(productId);
        Integer availableQuantity = inventoryService.getAvailableQuantity(productId);
        
        InventoryStatusResponse response = InventoryStatusResponse.of(
            productId, 
            inventory.getStatus().name(), 
            inventory.getQuantity(), 
            inventory.getReservedQuantity(), 
            availableQuantity
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get inventory information for multiple products in batch
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Inventory>> getInventoryBatch(@RequestBody List<String> productIds) {
        log.info("Batch inventory information request - Product IDs: {}", productIds);
        
        Map<String, Inventory> inventories = inventoryService.findByProductIds(productIds);
        return ResponseEntity.ok(inventories);
    }

    /**
     * Reserve stock
     */
    @PostMapping("/reserve")
    public ResponseEntity<String> reserveStock(@Valid @RequestBody StockReserveRequest request) {
        log.info("Stock reservation request - Product ID: {}, Quantity: {}", request.productId(), request.quantity());
        
        inventoryService.reserveStock(request.productId(), request.quantity());
        return ResponseEntity.ok("Stock reservation completed");
    }

    /**
     * Release reserved stock
     */
    @PostMapping("/release")
    public ResponseEntity<String> releaseStock(@Valid @RequestBody StockReleaseRequest request) {
        log.info("Release reserved stock request - Product ID: {}, Quantity: {}", request.productId(), request.quantity());
        
        inventoryService.releaseStock(request.productId(), request.quantity());
        return ResponseEntity.ok("Stock reservation released");
    }

    /**
     * Stock in process
     */
    @PostMapping("/stock-in")
    public ResponseEntity<String> stockIn(@Valid @RequestBody StockInRequest request) {
        log.info("Stock in process request - Product ID: {}, Quantity: {}", request.productId(), request.quantity());
        
        inventoryService.stockIn(request.productId(), request.quantity());
        return ResponseEntity.ok("Stock in process completed");
    }

    /**
     * Stock out process
     */
    @PostMapping("/stock-out")
    public ResponseEntity<String> stockOut(@Valid @RequestBody StockOutRequest request) {
        log.info("Stock out process request - Product ID: {}, Quantity: {}", request.productId(), request.quantity());
        
        inventoryService.stockOut(request.productId(), request.quantity());
        return ResponseEntity.ok("Stock out process completed");
    }

    /**
     * Get list of low stock items
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockItems(
            @RequestParam(defaultValue = "5") @Min(0) Integer threshold) {
        log.info("Low stock items request - Threshold: {}", threshold);
        
        List<Inventory> lowStockItems = inventoryService.findLowStockItems(threshold);
        return ResponseEntity.ok(lowStockItems);
    }

    // リクエスト・レスポンスクラス
    public record StockReserveRequest(
        @NotBlank String productId,
        @Min(1) Integer quantity
    ) {}

    public record StockReleaseRequest(
        @NotBlank String productId,
        @Min(1) Integer quantity
    ) {}

    public record StockInRequest(
        @NotBlank String productId,
        @Min(1) Integer quantity
    ) {}

    public record StockOutRequest(
        @NotBlank String productId,
        @Min(1) Integer quantity
    ) {}

    public record InventoryStatusResponse(
        String productId,
        String status,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity,
        Boolean inStock
    ) {
        // Alternative to builder pattern using Java 21 record
        public static InventoryStatusResponse of(String productId, String status, Integer quantity, 
                                                 Integer reservedQuantity, Integer availableQuantity) {
            return new InventoryStatusResponse(
                productId, 
                status, 
                quantity, 
                reservedQuantity, 
                availableQuantity, 
                availableQuantity > 0
            );
        }
    }
}
