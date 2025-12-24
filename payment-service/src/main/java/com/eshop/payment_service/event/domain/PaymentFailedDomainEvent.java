package com.eshop.payment_service.event.domain;

public record PaymentFailedDomainEvent(String orderNumber, String reason) {
}
