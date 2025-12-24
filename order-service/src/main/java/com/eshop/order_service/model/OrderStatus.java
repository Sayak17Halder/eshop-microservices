package com.eshop.order_service.model;

public enum OrderStatus {
    CREATED,
    WAITING_FOR_INVENTORY,
    INVENTORY_RESERVED,
    CANCELLED,
    CONFIRMED
}
