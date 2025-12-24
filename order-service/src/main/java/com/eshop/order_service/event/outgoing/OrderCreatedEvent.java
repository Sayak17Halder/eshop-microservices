package com.eshop.order_service.event.outgoing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    private String orderNumber;
    private BigDecimal amount;
    private List<OrderLineItemEvent> orderItems;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderLineItemEvent {
        private String skuCode;
        private Integer quantity;
        private BigDecimal price;
    }
}