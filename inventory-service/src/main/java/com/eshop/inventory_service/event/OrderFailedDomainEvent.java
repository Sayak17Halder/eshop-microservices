package com.eshop.inventory_service.event;

public record OrderFailedDomainEvent(String orderNumber, String reason) {
}
