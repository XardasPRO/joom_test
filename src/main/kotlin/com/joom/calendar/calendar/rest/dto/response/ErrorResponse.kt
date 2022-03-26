package com.joom.calendar.calendar.rest.dto.response

import com.joom.calendar.calendar.model.exception.ErrorCode

class ErrorResponse(errorCode: ErrorCode, val errorMessage: String?) : BaseResponse(errorCode)
{
}