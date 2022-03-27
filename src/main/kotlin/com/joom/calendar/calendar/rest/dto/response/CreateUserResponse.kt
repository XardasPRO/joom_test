package com.joom.calendar.calendar.rest.dto.response

import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.rest.dto.user.UserDto
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response for user create request")
class CreateUserResponse(errorCode: ErrorCode, val user: UserDto) : BaseResponse(errorCode) {
}