package io.wallet.config;

import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;
import java.util.UUID;

@Configuration
public class UuidConfig {

    @Bean
    public Supplier<UUID> uuidV7Generator() {
        return UuidCreator::getTimeOrderedEpoch;
    }
}
