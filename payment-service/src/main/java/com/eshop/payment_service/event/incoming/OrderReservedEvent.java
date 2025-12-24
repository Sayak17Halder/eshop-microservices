package com.eshop.payment_service.event.incoming;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReservedEvent {
    private String eventId;
    private Instant eventTimestamp;
    private String orderNumber;
    private BigDecimal amount;
}

