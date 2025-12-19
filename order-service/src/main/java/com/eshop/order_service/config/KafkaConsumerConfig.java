package com.eshop.order_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

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
}
