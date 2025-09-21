package com.skishop.inventory.exception;

/**
 * Base exception class for inventory management (using Java 21 sealed class)
 */
public sealed class InventoryException extends RuntimeException
    permits ResourceNotFoundException, InsufficientStockException, DuplicateResourceException {
    
    public InventoryException(String message) {
        super(message);
    }
    
    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
