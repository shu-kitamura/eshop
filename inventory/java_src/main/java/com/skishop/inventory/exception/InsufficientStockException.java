package com.skishop.inventory.exception;

/**
 * Insufficient stock exception (using sealed class)
 */
public final class InsufficientStockException extends InventoryException {
    
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
