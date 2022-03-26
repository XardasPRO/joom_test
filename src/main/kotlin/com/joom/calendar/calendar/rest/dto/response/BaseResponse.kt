package com.joom.calendar.calendar.rest.dto.response
import io.swagger.v3.oas.annotations.media.Schema
import com.joom.calendar.calendar.model.exception.ErrorCode

@Schema(description = "Base response body")
abstract class BaseResponse(
    @field:Schema(description = "Code of error")
    val errorCode: ErrorCode
)