package com.team1.mvp_test.common.exception

import com.team1.mvp_test.common.exception.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ModelNotFoundException::class)
    fun handleModelNotFound(e: ModelNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(e.message))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handlerIllegalStateException(e: IllegalStateException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse(e.message))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handlerIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(e.message))
    }

    @ExceptionHandler(PasswordIncorrectException::class)
    fun handlerPasswordIncorrectException(e: PasswordIncorrectException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(e.message))
    }

    @ExceptionHandler(NoPermissionException::class)
    fun handlerNoPermissionException(e: NoPermissionException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(e.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldError = e.fieldError
        val message = "${fieldError?.field}의 입력 값 [${fieldError?.rejectedValue}] 이 유효하지 않습니다"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(message))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameter(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse("필수 파라미터 '${e.parameterName}'이 누락되었습니다"))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse("Internal server error"))
    }
}
