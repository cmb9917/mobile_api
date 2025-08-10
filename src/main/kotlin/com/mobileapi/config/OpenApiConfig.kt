package com.mobileapi.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    @Value("\${server.servlet.context-path:/api}") private val contextPath: String
) {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Mobile API")
                    .description("""
                        A simple mobile backend API service providing hello world endpoints.
                        
                        ## Features
                        - Simple REST endpoints
                        - Comprehensive error handling
                        - OpenTelemetry observability
                        - Structured logging
                        
                        ## Error Handling
                        All responses include a retryable flag to indicate if the request can be safely retried.
                    """.trimIndent())
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("API Support")
                            .url("https://github.com/example/mobile-api")
                            .email("support@example.com")
                    )
                    .license(
                        License()
                            .name("MIT")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080$contextPath")
                        .description("Local development server"),
                    Server()
                        .url("https://api.example.com$contextPath")
                        .description("Production server")
                )
            )
    }
}