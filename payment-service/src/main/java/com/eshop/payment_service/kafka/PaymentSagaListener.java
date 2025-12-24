package com.eshop.payment_service.kafka;

import com.eshop.payment_service.event.domain.PaymentCompletedDomainEvent;
import com.eshop.payment_service.event.domain.PaymentFailedDomainEvent;
import com.eshop.payment_service.event.incoming.OrderReservedEvent;
import com.eshop.payment_service.model.Payment;
import com.eshop.payment_service.model.PaymentStatus;
import com.eshop.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSagaListener {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @KafkaListener(
            topics = "order-reserved",
            groupId = "payment-service-group"
    )
    @Transactional
    public void handleOrderReserved(OrderReservedEvent event) {

        log.info("Processing payment for order {}", event.getOrderNumber());

        // simulate payment
        boolean paymentSuccess = Math.random() > 0.2; // 80% success

        Payment payment = Payment.builder()
                .orderNumber(event.getOrderNumber())
                .amount(event.getAmount())
                .status(paymentSuccess ? PaymentStatus.COMPLETED : PaymentStatus.FAILED)
                .build();

        paymentRepository.save(payment);

        if (paymentSuccess) {
            paymentEventProducer.sendPaymentCompleted(new PaymentCompletedDomainEvent(event.getOrderNumber()));
        } else {
            paymentEventProducer.sendPaymentFailed(
                    new PaymentFailedDomainEvent(
                            event.getOrderNumber(),
                            "Payment gateway failure"
                    )
            );
        }
    }
}

