package com.eshop.order_service.event.incoming;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderReservedEvent {

    private String eventId;
    private Instant eventTimestamp;
    private String orderNumber;
    private BigDecimal amount;
}
