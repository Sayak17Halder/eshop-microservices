package com.eshop.order_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document("orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    @Id
    private String id;
    private String orderNumber;
    private BigDecimal amount;
    private List<OrderLineItem> orderItems;
    // saga
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
