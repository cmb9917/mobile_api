package com.mobileapi.controller

import com.mobileapi.dto.ApiResponse
import com.mobileapi.service.HelloService
import com.mobileapi.service.MetricsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/")
@Tag(name = "Hello API", description = "Simple hello world endpoints with Resilience4J patterns")
class HelloController(
    private val helloService: HelloService,
    private val metricsService: MetricsService
) {
    private val logger = LoggerFactory.getLogger(HelloController::class.java)

    @GetMapping("/hello")
    @Operation(
        summary = "Get hello message",
        description = "Returns a simple hello message"
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Successfully returned hello message",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            )
        ]
    )
    fun hello(): ResponseEntity<ApiResponse<String>> {
        return metricsService.timeOperation("/hello") {
            logger.info("Received request for /hello endpoint")
            
            val response = helloService.getHelloMessage()
            logger.info("Successfully processed /hello request")
            ResponseEntity.ok(response)
        }
    }

    @GetMapping("/hello_person")
    @Operation(
        summary = "Get personalized hello message",
        description = "Returns a personalized hello message with the provided name"
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Successfully returned personalized hello message",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Bad request - invalid name parameter",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            )
        ]
    )
    fun helloPerson(
        @Parameter(description = "Name of the person to greet", example = "John", required = true)
        @RequestParam name: String
    ): ResponseEntity<ApiResponse<String>> {
        return metricsService.timeOperation("/hello_person") {
            logger.info("Received request for /hello_person endpoint with name: {}", name)
            
            val response = helloService.getPersonalizedHello(name)
            
            // Return appropriate HTTP status based on response
            if (response.success) {
                logger.info("Successfully processed /hello_person request for name: {}", name)
                ResponseEntity.ok(response)
            } else {
                logger.warn("Failed to process /hello_person request for name: {} - {}", name, response.message)
                when (response.errorCode) {
                    "INVALID_PARAMETER" -> ResponseEntity.badRequest().body(response)
                    else -> ResponseEntity.internalServerError().body(response)
                }
            }
        }
    }

    @GetMapping("/external")
    @Operation(
        summary = "Call external service",
        description = "Demonstrates TimeLimiter, CircuitBreaker, and Retry patterns with an external service call"
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Successfully called external service",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "External service unavailable",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "408",
                description = "Request timeout",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiResponse::class)
                )]
            )
        ]
    )
    fun callExternalService(): ResponseEntity<ApiResponse<String>> {
        logger.info("Received request for /external endpoint")
        
        return try {
            val futureResponse = helloService.callExternalService()
            val response = futureResponse.get() // Block and wait for result
            
            if (response.success) {
                logger.info("Successfully processed /external request")
                ResponseEntity.ok(response)
            } else {
                logger.warn("Failed to process /external request - {}", response.message)
                when (response.errorCode) {
                    "EXTERNAL_SERVICE_UNAVAILABLE" -> ResponseEntity.internalServerError().body(response)
                    else -> ResponseEntity.status(408).body(response) // Timeout
                }
            }
        } catch (e: Exception) {
            logger.error("Unexpected error in /external endpoint", e)
            val errorResponse = ApiResponse.error<String>(
                message = "Unexpected error occurred",
                errorCode = "INTERNAL_ERROR",
                retryable = true
            )
            ResponseEntity.internalServerError().body(errorResponse)
        }
    }
}