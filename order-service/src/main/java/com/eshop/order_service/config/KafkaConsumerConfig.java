package com.eshop.order_service.config;

import com.eshop.order_service.event.incoming.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler errorHandler() {

        // Configure exponential retries
        ExponentialBackOffWithMaxRetries backoff =
                new ExponentialBackOffWithMaxRetries(5);     // retry 5 times

        backoff.setInitialInterval(2000);                 // 2 seconds
        backoff.setMultiplier(2.0);                      // exponential
        backoff.setMaxInterval(30000);                   // 30 seconds

        DefaultErrorHandler handler = new DefaultErrorHandler(backoff);

        // Log failures
        handler.setRetryListeners((record, ex, deliveryAttempt) -> {

            log.warn(
                    "Kafka retry attempt={} topic={} partition={} offset={} key={} error={}",
                    deliveryAttempt,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    ex.getMessage(),
                    ex
            );
        });

        return handler;
    }

    // ---------------- ORDER RESERVED EVENT ---------------- (@KafkaListener + containerFactory)
    @Bean
    public ConsumerFactory<String, OrderReservedEvent> orderReservedConsumerFactory(
            KafkaProperties properties
    ) {
        Map<String, Object> props = new HashMap<>(properties.buildConsumerProperties());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(OrderReservedEvent.class, false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderReservedEvent>
    orderReservedKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderReservedEvent> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderReservedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }

    // ---------------- ORDER FAILED EVENT ---------------- (@KafkaListener + containerFactory)

    @Bean
    public ConsumerFactory<String, OrderFailedEvent> orderFailedConsumerFactory(
            KafkaProperties properties
    ) {
        Map<String, Object> props = new HashMap<>(properties.buildConsumerProperties());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(OrderFailedEvent.class, false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderFailedEvent>
    orderFailedKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderFailedEvent> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderFailedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }

    // ---------------- PAYMENT COMPLETED EVENT ---------------- (@KafkaListener + containerFactory)
    @Bean
    public ConsumerFactory<String, PaymentCompletedEvent> paymentCompletedConsumerFactory(
            KafkaProperties properties
    ) {
        Map<String, Object> props = new HashMap<>(properties.buildConsumerProperties());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(PaymentCompletedEvent.class, false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent>
    paymentCompletedKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentCompletedEvent> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }

    // ---------------- PAYMENT FAILED EVENT ---------------- (@KafkaListener + containerFactory)

    @Bean
    public ConsumerFactory<String, PaymentFailedEvent> paymentFailedConsumerFactory(
            KafkaProperties properties
    ) {
        Map<String, Object> props = new HashMap<>(properties.buildConsumerProperties());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(PaymentFailedEvent.class, false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent>
    paymentFailedKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentFailedEvent> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }

    // ---------------- INVENTORY RELEASED EVENT ---------------- (@KafkaListener + containerFactory)

    @Bean
    public ConsumerFactory<String, InventoryReleasedEvent> inventoryReleasedConsumerFactory(
            KafkaProperties properties
    ) {
        Map<String, Object> props = new HashMap<>(properties.buildConsumerProperties());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(InventoryReleasedEvent.class, false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReleasedEvent>
    inventoryReleasedKafkaListenerContainerFactory(
            ConsumerFactory<String, InventoryReleasedEvent> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, InventoryReleasedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }
}
