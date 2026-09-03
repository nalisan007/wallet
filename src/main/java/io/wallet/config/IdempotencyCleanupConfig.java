package io.wallet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class IdempotencyCleanupConfig {

    @Bean
    public Duration idempotencyRetention(
        @Value("${wallet.idempotency.retention:PT24H}") Duration retention
    ) {
        return retention;
    }
}
