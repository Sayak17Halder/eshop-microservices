package com.eshop.order_service.kafka;

import com.eshop.order_service.event.OrderFailedEvent;
import com.eshop.order_service.event.OrderReservedEvent;
import com.eshop.order_service.exception.OrderNotFoundException;
import com.eshop.order_service.model.Order;
import com.eshop.order_service.model.OrderStatus;
import com.eshop.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventorySagaListener {

    private final OrderRepository orderRepository;

    // ---------- SUCCESS PATH ----------
    @KafkaListener(
            topics = "order-reserved",
            groupId = "order-service-group",
            containerFactory = "orderReservedKafkaListenerContainerFactory",
            concurrency = "3"
    )
    @Transactional
    public void handleStockReserved(OrderReservedEvent event) {

        log.info("⬅ OrderReservedEvent received for order {}", event.getOrderNumber());

        Order order = orderRepository.findByOrderNumber(event.getOrderNumber())
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found for orderNumber=" + event.getOrderNumber()
                ));

        // Idempotency check
        if (order.getStatus() == OrderStatus.INVENTORY_RESERVED) {
            log.warn("⚠ Order {} already marked INVENTORY_RESERVED", event.getOrderNumber());
            return;
        }

        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);

        log.info("✔ Order {} marked as INVENTORY_RESERVED", event.getOrderNumber());
    }

    // ---------- FAILURE PATH ----------
    @KafkaListener(
            topics = "order-failed",
            groupId = "order-service-group",
            containerFactory = "orderFailedKafkaListenerContainerFactory",
            concurrency = "3"
    )
    @Transactional
    public void handleStockFailed(OrderFailedEvent event) {

        log.info("⬅ OrderFailedEvent received for order {}", event.getOrderNumber());

        Order order = orderRepository.findByOrderNumber(event.getOrderNumber())
                .orElseThrow(() -> new IllegalStateException(
                        "Order not found for orderNumber=" + event.getOrderNumber()
                ));

        // Idempotency check
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("⚠ Order {} already CANCELLED", event.getOrderNumber());
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);

        log.info("❌ Order {} cancelled due to inventory failure. Reason={}",
                event.getOrderNumber(), event.getReason());
    }
}
