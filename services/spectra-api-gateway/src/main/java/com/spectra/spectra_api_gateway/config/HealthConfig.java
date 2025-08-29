package com.spectra.spectra_api_gateway.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.amqp.RabbitHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthConfig {

    @Bean
    public HealthIndicator rabbitHealthIndicator(RabbitTemplate rabbitTemplate) {
        return new RabbitHealthIndicator(rabbitTemplate);
    }

    @Bean
    public HealthIndicator customHealth() {
        return () -> Health.up().withDetail("service", "Spectra API Gateway").build();
    }
}
