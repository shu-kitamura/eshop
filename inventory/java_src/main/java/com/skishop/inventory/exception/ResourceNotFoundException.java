package com.skishop.inventory.exception;

/**
 * Resource not found exception (using sealed class)
 */
public final class ResourceNotFoundException extends InventoryException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
