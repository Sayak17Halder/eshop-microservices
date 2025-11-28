package com.eshop.order_service.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderLineItem {

    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
}