package com.eshop.inventory_service.exception;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(String msg) {
        super(msg);
    }
}