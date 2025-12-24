package com.eshop.order_service.kafka;

import com.eshop.order_service.event.incoming.PaymentCompletedEvent;
import com.eshop.order_service.event.incoming.PaymentFailedEvent;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSagaListener {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "payment-completed",
            groupId = "payment-service-group",
            containerFactory = "paymentCompletedKafkaListenerContainerFactory")
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {

        log.info("⬅ Received PaymentCompletedEvent received for order {}", event.getOrderNumber());

        Order order = orderRepository.findByOrderNumber(event.getOrderNumber())
                .orElseThrow(() -> new OrderNotFoundException(
                        "Payment not found for orderNumber=" + event.getOrderNumber()
                ));

        // Idempotency check
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            log.warn("⚠ Order {} already marked CONFIRMED", event.getOrderNumber());
            return;
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);

        log.info("✔ Payment {} marked as CONFIRMED", event.getOrderNumber());
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "payment-service-group",
            containerFactory = "paymentFailedKafkaListenerContainerFactory")
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {

        log.info("⬅ Received PaymentFailedEvent received for order {}", event.getOrderNumber());

        Order order = orderRepository.findByOrderNumber(event.getOrderNumber())
                .orElseThrow(() -> new OrderNotFoundException("Payment not found for orderNumber=" + event.getOrderNumber()));

        // Idempotency check
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("⚠ Payment {} already CANCELLED", event.getOrderNumber());
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);

        log.info("❌ Order {} cancelled due to payment failure. Reason={}",
                event.getOrderNumber(), event.getReason());
    }
}
