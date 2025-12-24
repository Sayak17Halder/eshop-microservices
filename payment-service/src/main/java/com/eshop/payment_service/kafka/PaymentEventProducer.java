package com.eshop.payment_service.kafka;

import com.eshop.payment_service.event.domain.PaymentCompletedDomainEvent;
import com.eshop.payment_service.event.domain.PaymentFailedDomainEvent;
import com.eshop.payment_service.event.outgoing.PaymentCompletedEvent;
import com.eshop.payment_service.event.outgoing.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPaymentCompleted(PaymentCompletedDomainEvent event) {
        kafkaTemplate.send(
                "payment-completed",
                event.orderNumber(),
                event
        );
        log.info("Published PaymentCompletedEvent for order {}", event.orderNumber());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPaymentFailed(PaymentFailedDomainEvent event) {
        kafkaTemplate.send(
                "payment-failed",
                event.orderNumber(),
                event
        );
        log.info("Published PaymentFailedCompleted for order {}", event.orderNumber());
    }
}

