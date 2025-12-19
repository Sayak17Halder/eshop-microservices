package com.eshop.inventory_service.kafka;

import com.eshop.inventory_service.event.OrderFailedDomainEvent;
import com.eshop.inventory_service.event.OrderFailedEvent;
import com.eshop.inventory_service.event.OrderReservedDomainEvent;
import com.eshop.inventory_service.event.OrderReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishOrderReservedEvent(OrderReservedDomainEvent event) {

        OrderReservedEvent orderReservedEvent = OrderReservedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventTimestamp(Instant.now())
                .orderNumber(event.orderNumber())
                .build();

        kafkaTemplate.send("order-reserved", event.orderNumber(), orderReservedEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("➡ Published StockReservedEvent for order {} to topic {}",
                                event.orderNumber(), result.getRecordMetadata().topic());
                    } else {
                        log.error("❌ Failed to publish StockReservedEvent for order {}", event.orderNumber(), ex);
                    }
                });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishOrderFailedEvent(OrderFailedDomainEvent event) {

        OrderFailedEvent orderFailedEvent = OrderFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventTimestamp(Instant.now())
                .orderNumber(event.orderNumber())
                .reason(event.reason())
                .build();

        kafkaTemplate.send("order-failed", event.orderNumber(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("➡ Published StockFailedEvent for order {} to topic {}",
                                event.orderNumber(), result.getRecordMetadata().topic());
                    } else {
                        log.error("❌ Failed to publish StockFailedEvent for order {}", event.orderNumber(), ex);
                    }
                });
    }
}

