package com.joom.calendar.calendar.rest.dto.response

import com.joom.calendar.calendar.model.exception.ErrorCode
import java.time.ZonedDateTime

class TimeResponse(errorCode: ErrorCode, val closestDateTime: ZonedDateTime) : BaseResponse(errorCode) {
}