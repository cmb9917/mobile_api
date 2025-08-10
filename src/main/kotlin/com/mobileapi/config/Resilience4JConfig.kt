package com.mobileapi.config

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics
import io.github.resilience4j.micrometer.tagged.TaggedTimeLimiterMetrics
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import io.github.resilience4j.retry.RetryRegistry
import io.github.resilience4j.timelimiter.TimeLimiterRegistry
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Resilience4JConfig {

    private val logger = LoggerFactory.getLogger(Resilience4JConfig::class.java)

    @Bean
    fun taggedCircuitBreakerMetrics(
        circuitBreakerRegistry: CircuitBreakerRegistry,
        meterRegistry: MeterRegistry
    ): TaggedCircuitBreakerMetrics {
        logger.info("Configuring CircuitBreaker metrics integration")
        val metrics = TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry)
        metrics.bindTo(meterRegistry)
        return metrics
    }

    @Bean
    fun taggedRetryMetrics(
        retryRegistry: RetryRegistry,
        meterRegistry: MeterRegistry
    ): TaggedRetryMetrics {
        logger.info("Configuring Retry metrics integration")
        val metrics = TaggedRetryMetrics.ofRetryRegistry(retryRegistry)
        metrics.bindTo(meterRegistry)
        return metrics
    }

    @Bean
    fun taggedRateLimiterMetrics(
        rateLimiterRegistry: RateLimiterRegistry,
        meterRegistry: MeterRegistry
    ): TaggedRateLimiterMetrics {
        logger.info("Configuring RateLimiter metrics integration")
        val metrics = TaggedRateLimiterMetrics.ofRateLimiterRegistry(rateLimiterRegistry)
        metrics.bindTo(meterRegistry)
        return metrics
    }

    @Bean
    fun taggedTimeLimiterMetrics(
        timeLimiterRegistry: TimeLimiterRegistry,
        meterRegistry: MeterRegistry
    ): TaggedTimeLimiterMetrics {
        logger.info("Configuring TimeLimiter metrics integration")
        val metrics = TaggedTimeLimiterMetrics.ofTimeLimiterRegistry(timeLimiterRegistry)
        metrics.bindTo(meterRegistry)
        return metrics
    }
}