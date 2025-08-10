package com.mobileapi.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Standard API response wrapper")
data class ApiResponse<T>(
    @Schema(description = "Response data", example = "hello")
    val data: T? = null,
    
    @Schema(description = "Success indicator", example = "true")
    val success: Boolean = true,
    
    @Schema(description = "Error message if applicable", example = "null")
    val message: String? = null,
    
    @Schema(description = "Error code if applicable", example = "null")
    val errorCode: String? = null,
    
    @Schema(description = "Indicates if request is retryable", example = "false")
    val retryable: Boolean = false,
    
    @Schema(description = "Request timestamp", example = "2023-12-01T10:30:00Z")
    val timestamp: String = java.time.Instant.now().toString()
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(data = data)
        
        fun <T> error(message: String, errorCode: String, retryable: Boolean = false): ApiResponse<T> = 
            ApiResponse(
                data = null,
                success = false,
                message = message,
                errorCode = errorCode,
                retryable = retryable
            )
    }
}