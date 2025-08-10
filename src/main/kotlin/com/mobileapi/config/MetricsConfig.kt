package com.mobileapi.config

import org.springframework.context.annotation.Configuration

/**
 * Metrics configuration using Spring Boot's auto-configured MeterRegistry.
 * OTLP export is configured via application.yml properties.
 */
@Configuration
class MetricsConfig {
    // Spring Boot will auto-configure the MeterRegistry based on application.yml
    // No custom beans needed - using management.otlp.metrics.export.* properties
}