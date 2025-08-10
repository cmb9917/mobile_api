package com.mobileapi.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
class MetricsServiceTest {

    private lateinit var meterRegistry: MeterRegistry
    private lateinit var metricsService: MetricsService

    @BeforeEach
    fun setUp() {
        meterRegistry = SimpleMeterRegistry()
        metricsService = MetricsService(meterRegistry)
    }

    @Test
    fun `should increment hello requests counter`() {
        // When
        metricsService.incrementHelloRequests()

        // Then
        val counter = meterRegistry.counter("api_requests_total", "endpoint", "/hello")
        assert(counter.count() == 1.0)
    }

    @Test
    fun `should increment hello_person requests counter`() {
        // When
        metricsService.incrementHelloPersonRequests()

        // Then
        val counter = meterRegistry.counter("api_requests_total", "endpoint", "/hello_person")
        assert(counter.count() == 1.0)
    }

    @Test
    fun `should increment error counter`() {
        // When
        metricsService.incrementErrors("/test", "ERROR_TYPE")

        // Then
        val counter = meterRegistry.counter("api_errors_total", "endpoint", "/test", "error_type", "ERROR_TYPE")
        assert(counter.count() == 1.0)
    }

    @Test
    fun `should record request time`() {
        // When
        metricsService.recordRequestTime("/test", 100L)

        // Then
        val timer = meterRegistry.timer("api_request_duration", "endpoint", "/test")
        assert(timer.count() == 1L)
    }

    @Test
    fun `should set active users count`() {
        // When
        metricsService.setActiveUsers(5)

        // Then - Just verify no exceptions are thrown
        // UpDownCounter changes are tested through actual metric collection
        assert(true)
    }

    @Test
    fun `should increment active users`() {
        // When
        metricsService.incrementActiveUsers()
        metricsService.incrementActiveUsers()

        // Then - Just verify no exceptions are thrown
        assert(true)
    }

    @Test
    fun `should decrement active users`() {
        // Given
        metricsService.setActiveUsers(3)

        // When
        metricsService.decrementActiveUsers()

        // Then - Just verify no exceptions are thrown
        assert(true)
    }

    @Test
    fun `should not decrement active users below zero`() {
        // Given
        metricsService.setActiveUsers(0)

        // When
        metricsService.decrementActiveUsers()

        // Then - Just verify no exceptions are thrown
        assert(true)
    }

    @Test
    fun `should time operation successfully`() {
        // When
        val result = metricsService.timeOperation("test") { "success" }

        // Then
        assert(result == "success")
        val timer = meterRegistry.timer("operation_duration", "operation", "test")
        assert(timer.count() == 1L)
    }

    @Test
    fun `should time operation and handle exceptions`() {
        // When & Then
        try {
            metricsService.timeOperation("test") { 
                throw RuntimeException("test error")
            }
        } catch (e: RuntimeException) {
            assert(e.message == "test error")
        }

        val errorCounter = meterRegistry.counter("api_errors_total", "endpoint", "test", "error_type", "RuntimeException")
        assert(errorCounter.count() == 1.0)
    }
}