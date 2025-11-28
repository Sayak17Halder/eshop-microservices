package com.eshop.order_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    private BigDecimal amount;
    private List<OrderItemRequest> orderItems;
}
