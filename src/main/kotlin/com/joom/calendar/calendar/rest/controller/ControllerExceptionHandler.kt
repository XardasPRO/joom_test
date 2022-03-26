package com.joom.calendar.calendar.rest.controller

import com.joom.calendar.calendar.model.exception.BaseException
import com.joom.calendar.calendar.rest.dto.response.BaseResponse
import com.joom.calendar.calendar.rest.dto.response.ErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ControllerExceptionHandler {

    @ExceptionHandler(BaseException::class)
    fun handleException(exception: BaseException): ResponseEntity<BaseResponse> {

        return ResponseEntity.internalServerError().body(
            ErrorResponse(exception.errorCode, exception.message)
        )
    }
}