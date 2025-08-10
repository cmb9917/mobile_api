package com.mobileapi.exception

import com.mobileapi.dto.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.warn("Invalid argument provided: {}", ex.message)
        
        val response = ApiResponse.error<Any>(
            message = ex.message ?: "Invalid argument provided",
            errorCode = "INVALID_ARGUMENT",
            retryable = false
        )
        
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(
        ex: MissingServletRequestParameterException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.warn("Missing required parameter: {}", ex.parameterName)
        
        val response = ApiResponse.error<Any>(
            message = "Missing required parameter: ${ex.parameterName}",
            errorCode = "MISSING_PARAMETER",
            retryable = false
        )
        
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        ex: MethodArgumentTypeMismatchException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.warn("Invalid parameter type for {}: {}", ex.name, ex.value)
        
        val response = ApiResponse.error<Any>(
            message = "Invalid parameter type for ${ex.name}",
            errorCode = "INVALID_PARAMETER_TYPE",
            retryable = false
        )
        
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        ex: RuntimeException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.error("Runtime error occurred: {}", ex.message, ex)
        
        val response = ApiResponse.error<Any>(
            message = "An internal error occurred",
            errorCode = "INTERNAL_ERROR",
            retryable = true
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.error("Unexpected error occurred: {}", ex.message, ex)
        
        val response = ApiResponse.error<Any>(
            message = "An unexpected error occurred",
            errorCode = "UNEXPECTED_ERROR",
            retryable = true
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}