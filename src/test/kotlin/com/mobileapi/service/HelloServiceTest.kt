package com.mobileapi.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@SpringBootTest
@ActiveProfiles("test")
class HelloServiceTest {

    @Autowired
    private lateinit var helloService: HelloService

    @MockkBean
    private lateinit var metricsService: MetricsService

    @Test
    fun `should return success response from getHelloMessage`() {
        // Given
        justRun { metricsService.incrementHelloRequests() }

        // When
        val result = helloService.getHelloMessage()

        // Then - Should succeed since random failures are disabled in test
        assertTrue(result.success)
        assertEquals("hello", result.data)
        assertNull(result.message)
        assertNull(result.errorCode)
        assertFalse(result.retryable)
        verify { metricsService.incrementHelloRequests() }
    }

    @Test
    fun `should return success response for valid name in getPersonalizedHello`() {
        // Given
        val name = "John"
        justRun { metricsService.incrementHelloPersonRequests() }
        justRun { metricsService.incrementActiveUsers() }

        // When
        val result = helloService.getPersonalizedHello(name)

        // Then - Should succeed since random failures are disabled in test
        assertTrue(result.success)
        assertEquals("hello John", result.data)
        assertNull(result.message)
        assertNull(result.errorCode)
        assertFalse(result.retryable)
        verify { metricsService.incrementHelloPersonRequests() }
        verify { metricsService.incrementActiveUsers() }
    }

    @Test
    fun `should return error response for empty name in getPersonalizedHello`() {
        // Given
        val emptyName = ""
        justRun { metricsService.incrementHelloPersonRequests() }
        justRun { metricsService.incrementErrors(any(), any()) }

        // When
        val result = helloService.getPersonalizedHello(emptyName)

        // Then
        assertFalse(result.success)
        assertEquals("Name parameter cannot be empty", result.message)
        assertEquals("INVALID_PARAMETER", result.errorCode)
        assertFalse(result.retryable)
        verify { metricsService.incrementHelloPersonRequests() }
        verify { metricsService.incrementErrors("/hello_person", "INVALID_PARAMETER") }
    }

    @Test
    fun `should return error response for blank name in getPersonalizedHello`() {
        // Given
        val blankName = "   "
        justRun { metricsService.incrementHelloPersonRequests() }
        justRun { metricsService.incrementErrors(any(), any()) }

        // When
        val result = helloService.getPersonalizedHello(blankName)

        // Then
        assertFalse(result.success)
        assertEquals("Name parameter cannot be empty", result.message)
        assertEquals("INVALID_PARAMETER", result.errorCode)
        assertFalse(result.retryable)
    }

    @Test
    fun `should complete external service call successfully in test mode`() {
        // When
        val futureResult = helloService.callExternalService()

        // Then - Should succeed in test mode (no random failures or delays)
        assertDoesNotThrow {
            val result = futureResult.get(5, TimeUnit.SECONDS)
            assertNotNull(result)
            assertTrue(result.success)
            assertTrue(result.data!!.startsWith("External service response:"))
        }
    }

    @Test
    fun `fallback methods should return appropriate error responses`() {
        // Given
        justRun { metricsService.incrementErrors(any(), any()) }
        
        // Test hello fallback
        val helloFallback = helloService.helloFallback(RuntimeException("Test exception"))
        assertFalse(helloFallback.success)
        assertEquals("Hello service is temporarily unavailable. Please try again later.", helloFallback.message)
        assertEquals("SERVICE_UNAVAILABLE", helloFallback.errorCode)
        assertTrue(helloFallback.retryable)

        // Test hello person fallback
        val helloPersonFallback = helloService.helloPersonFallback("John", RuntimeException("Test exception"))
        assertFalse(helloPersonFallback.success)
        assertEquals("Personalized greeting service is temporarily unavailable. Please try again later.", helloPersonFallback.message)
        assertEquals("SERVICE_UNAVAILABLE", helloPersonFallback.errorCode)
        assertTrue(helloPersonFallback.retryable)

        // Test external service fallback
        val externalFallback = helloService.externalServiceFallback(RuntimeException("Test exception"))
        val externalResult = externalFallback.get()
        assertFalse(externalResult.success)
        assertEquals("External service is currently unavailable", externalResult.message)
        assertEquals("EXTERNAL_SERVICE_UNAVAILABLE", externalResult.errorCode)
        assertTrue(externalResult.retryable)
        
        // Verify metrics are called
        verify { metricsService.incrementErrors(any(), any()) }
    }
}