package com.mobileapi.service

import com.mobileapi.dto.ApiResponse
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import io.github.resilience4j.retry.annotation.Retry
import io.github.resilience4j.timelimiter.annotation.TimeLimiter
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random

@Service
class HelloService(
    private val metricsService: MetricsService,
    private val environment: Environment
) {
    
    private val logger = LoggerFactory.getLogger(HelloService::class.java)
    
    @CircuitBreaker(name = "hello-service", fallbackMethod = "helloFallback")
    @Retry(name = "hello-service")
    @RateLimiter(name = "hello-service")
    fun getHelloMessage(): ApiResponse<String> {
        logger.info("Processing hello request with Resilience4J patterns")
        metricsService.incrementHelloRequests()
        
        // Simulate potential failure for demonstration
        simulateRandomFailure("hello")
        
        val response = ApiResponse.success("hello")
        logger.info("Successfully processed hello request")
        return response
    }
    
    @CircuitBreaker(name = "hello-person-service", fallbackMethod = "helloPersonFallback")
    @Retry(name = "hello-person-service")
    @RateLimiter(name = "hello-person-service")
    fun getPersonalizedHello(name: String): ApiResponse<String> {
        logger.info("Processing hello_person request for name: {} with Resilience4J patterns", name)
        metricsService.incrementHelloPersonRequests()
        
        if (name.isBlank()) {
            logger.warn("Empty name parameter provided")
            metricsService.incrementErrors("/hello_person", "INVALID_PARAMETER")
            return ApiResponse.error(
                message = "Name parameter cannot be empty",
                errorCode = "INVALID_PARAMETER",
                retryable = false
            )
        }
        
        // Simulate potential failure for demonstration
        simulateRandomFailure("hello_person")
        
        // Simulate user activity for business metrics
        metricsService.incrementActiveUsers()
        
        val response = ApiResponse.success("hello $name")
        logger.info("Successfully processed hello_person request for name: {}", name)
        return response
    }
    
    @TimeLimiter(name = "external-service")
    @CircuitBreaker(name = "external-service", fallbackMethod = "externalServiceFallback")
    @Retry(name = "external-service")
    fun callExternalService(): CompletableFuture<ApiResponse<String>> {
        logger.info("Calling external service with time limiter")
        
        return CompletableFuture.supplyAsync {
            // Check if we're in test mode
            if (environment.activeProfiles.contains("test")) {
                // Simple test behavior - no delay or random failures
                logger.info("External service call in test mode")
                return@supplyAsync ApiResponse.success("External service response: ${LocalDateTime.now()}")
            }
            
            // Simulate external service call with variable delay
            val delay = ThreadLocalRandom.current().nextLong(100, 2000)
            Thread.sleep(delay)
            
            // Simulate potential failure
            if (Random.nextDouble() < 0.3) {
                logger.error("External service call failed")
                throw RuntimeException("External service unavailable")
            }
            
            logger.info("External service call completed successfully after {}ms", delay)
            ApiResponse.success("External service response: ${LocalDateTime.now()}")
        }
    }
    
    // Fallback methods
    fun helloFallback(exception: Exception): ApiResponse<String> {
        logger.warn("Hello service fallback triggered due to: {}", exception.message)
        metricsService.incrementErrors("/hello", "CIRCUIT_BREAKER_FALLBACK")
        return ApiResponse.error(
            message = "Hello service is temporarily unavailable. Please try again later.",
            errorCode = "SERVICE_UNAVAILABLE",
            retryable = true
        )
    }
    
    fun helloPersonFallback(name: String, exception: Exception): ApiResponse<String> {
        logger.warn("Hello person service fallback triggered for name: {} due to: {}", name, exception.message)
        metricsService.incrementErrors("/hello_person", "CIRCUIT_BREAKER_FALLBACK")
        return ApiResponse.error(
            message = "Personalized greeting service is temporarily unavailable. Please try again later.",
            errorCode = "SERVICE_UNAVAILABLE",
            retryable = true
        )
    }
    
    fun externalServiceFallback(exception: Exception): CompletableFuture<ApiResponse<String>> {
        logger.warn("External service fallback triggered due to: {}", exception.message)
        metricsService.incrementErrors("external_service", "CIRCUIT_BREAKER_FALLBACK")
        return CompletableFuture.completedFuture(
            ApiResponse.error(
                message = "External service is currently unavailable",
                errorCode = "EXTERNAL_SERVICE_UNAVAILABLE",
                retryable = true
            )
        )
    }
    
    // Helper method to simulate failures for demonstration purposes
    private fun simulateRandomFailure(operation: String) {
        // Skip simulation in test profile
        if (environment.activeProfiles.contains("test")) {
            return
        }
        
        val failureRate = when (operation) {
            "hello" -> 0.1 // 10% failure rate
            "hello_person" -> 0.15 // 15% failure rate
            else -> 0.05
        }
        
        if (Random.nextDouble() < failureRate) {
            logger.warn("Simulating failure for operation: {}", operation)
            metricsService.incrementErrors(operation, "SIMULATED_FAILURE")
            throw RuntimeException("Simulated service failure for demonstration")
        }
    }
}