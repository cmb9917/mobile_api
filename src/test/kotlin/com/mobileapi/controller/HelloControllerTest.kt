package com.mobileapi.controller

import com.mobileapi.dto.ApiResponse
import com.mobileapi.service.HelloService
import com.mobileapi.service.MetricsService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(HelloController::class)
@ActiveProfiles("test")
class HelloControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var helloService: HelloService

    @MockkBean
    private lateinit var metricsService: MetricsService

    @Test
    fun `should return hello message when calling hello endpoint`() {
        // Given
        every { metricsService.timeOperation(any(), any<() -> Any>()) } answers { 
            val operation = secondArg<() -> Any>()
            operation.invoke()
        }
        every { helloService.getHelloMessage() } returns ApiResponse.success("hello")

        // When & Then
        mockMvc.perform(get("/hello"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("hello"))
            .andExpect(jsonPath("$.message").isEmpty)
            .andExpect(jsonPath("$.errorCode").isEmpty)
            .andExpect(jsonPath("$.retryable").value(false))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun `should return personalized hello message when calling hello_person endpoint`() {
        // Given
        val name = "John"
        every { metricsService.timeOperation(any(), any<() -> Any>()) } answers { 
            val operation = secondArg<() -> Any>()
            operation.invoke()
        }
        every { helloService.getPersonalizedHello(name) } returns ApiResponse.success("hello John")

        // When & Then
        mockMvc.perform(get("/hello_person").param("name", name))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("hello John"))
            .andExpect(jsonPath("$.message").isEmpty)
            .andExpect(jsonPath("$.errorCode").isEmpty)
            .andExpect(jsonPath("$.retryable").value(false))
    }

    @Test
    fun `should return bad request when name parameter is missing`() {
        // When & Then
        mockMvc.perform(get("/hello_person"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when name parameter is empty`() {
        // Given
        every { metricsService.timeOperation(any(), any<() -> Any>()) } answers { 
            val operation = secondArg<() -> Any>()
            operation.invoke()
        }
        every { helloService.getPersonalizedHello("") } returns ApiResponse.error(
            message = "Name parameter cannot be empty",
            errorCode = "INVALID_PARAMETER",
            retryable = false
        )

        // When & Then
        mockMvc.perform(get("/hello_person").param("name", ""))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Name parameter cannot be empty"))
            .andExpect(jsonPath("$.errorCode").value("INVALID_PARAMETER"))
            .andExpect(jsonPath("$.retryable").value(false))
    }

    @Test
    fun `should return response from external service endpoint`() {
        // Given
        val expectedResponse = ApiResponse.success("External service response: test")
        every { helloService.callExternalService() } returns java.util.concurrent.CompletableFuture.completedFuture(expectedResponse)

        // When & Then
        mockMvc.perform(get("/external"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("External service response: test"))
    }
    
    @Test
    fun `should handle external service failure gracefully`() {
        // Given
        val errorResponse = ApiResponse.error<String>(
            message = "External service is currently unavailable",
            errorCode = "EXTERNAL_SERVICE_UNAVAILABLE", 
            retryable = true
        )
        every { helloService.callExternalService() } returns java.util.concurrent.CompletableFuture.completedFuture(errorResponse)

        // When & Then
        mockMvc.perform(get("/external"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("External service is currently unavailable"))
            .andExpect(jsonPath("$.errorCode").value("EXTERNAL_SERVICE_UNAVAILABLE"))
    }

}