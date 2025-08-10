package com.mobileapi.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong

@Service
class MetricsService(private val meterRegistry: MeterRegistry) {
    
    private val logger = LoggerFactory.getLogger(MetricsService::class.java)
    
    // Counters for API endpoints
    private val apiRequestsCounter = Counter.builder("api_requests_total")
        .description("Total number of API requests")
        .register(meterRegistry)
    
    // Counter for errors
    private val apiErrorsCounter = Counter.builder("api_errors_total")
        .description("Total number of API errors")
        .register(meterRegistry)
    
    // Timer for request durations
    private val requestDurationTimer = Timer.builder("api_request_duration")
        .description("Request processing time")
        .register(meterRegistry)
    
    // Timer for operation durations
    private val operationDurationTimer = Timer.builder("operation_duration")
        .description("Operation execution time")
        .register(meterRegistry)
    
    // Atomic counter for active users (business metric)
    private val activeUsersCount = AtomicLong(0)
    
    init {
        // Register gauge for active users
        meterRegistry.gauge("business_active_users", activeUsersCount) { it.get().toDouble() }
    }
    
    // API request metrics methods
    fun incrementHelloRequests() {
        Counter.builder("api_requests_total")
            .description("Total number of API requests")
            .tag("endpoint", "/hello")
            .register(meterRegistry)
            .increment()
        logger.debug("Incremented hello requests counter")
    }
    
    fun incrementHelloPersonRequests() {
        Counter.builder("api_requests_total")
            .description("Total number of API requests")
            .tag("endpoint", "/hello_person")
            .register(meterRegistry)
            .increment()
        logger.debug("Incremented hello_person requests counter")
    }
    
    fun incrementErrors(endpoint: String, errorType: String) {
        Counter.builder("api_errors_total")
            .description("Total number of API errors")
            .tag("endpoint", endpoint)
            .tag("error_type", errorType)
            .register(meterRegistry)
            .increment()
        logger.debug("Incremented error counter for endpoint: {}, error: {}", endpoint, errorType)
    }
    
    fun recordRequestTime(endpoint: String, duration: Long) {
        Timer.builder("api_request_duration")
            .description("Request processing time")
            .tag("endpoint", endpoint)
            .register(meterRegistry)
            .record(Duration.ofMillis(duration))
        logger.debug("Recorded request duration for {}: {}ms", endpoint, duration)
    }
    
    // Business metric methods
    fun setActiveUsers(count: Long) {
        activeUsersCount.set(count)
        logger.debug("Updated active users count to: {}", count)
    }
    
    fun incrementActiveUsers() {
        val newCount = activeUsersCount.incrementAndGet()
        logger.debug("Incremented active users to: {}", newCount)
    }
    
    fun decrementActiveUsers() {
        val currentCount = activeUsersCount.get()
        if (currentCount > 0) {
            val newCount = activeUsersCount.decrementAndGet()
            logger.debug("Decremented active users to: {}", newCount)
        }
    }
    
    // Helper method to time operations
    fun <T> timeOperation(operationName: String, operation: () -> T): T {
        return Timer.builder("operation_duration")
            .description("Operation execution time")
            .tag("operation", operationName)
            .register(meterRegistry)
            .recordCallable {
                try {
                    operation()
                } catch (e: Exception) {
                    incrementErrors(operationName, e.javaClass.simpleName)
                    throw e
                }
            }!!
    }
}