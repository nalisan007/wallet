package io.wallet.config;

import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

import jakarta.annotation.PostConstruct;

@Configuration
public class DateTimeConfig {

    @PostConstruct
    public void configureUtcTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
