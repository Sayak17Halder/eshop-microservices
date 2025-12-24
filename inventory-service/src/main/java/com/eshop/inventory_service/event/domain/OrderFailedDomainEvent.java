package com.eshop.inventory_service.event.domain;

public record OrderFailedDomainEvent(String orderNumber, String reason) {
}
