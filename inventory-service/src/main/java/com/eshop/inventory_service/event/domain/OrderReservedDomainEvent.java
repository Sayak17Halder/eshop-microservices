package com.eshop.inventory_service.event.domain;

import java.math.BigDecimal;

public record OrderReservedDomainEvent(String orderNumber, BigDecimal amount) {
}
