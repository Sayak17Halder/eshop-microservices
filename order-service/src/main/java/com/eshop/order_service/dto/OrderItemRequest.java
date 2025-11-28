package com.eshop.order_service.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemRequest {
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
}