package com.joom.calendar.calendar.rest.dto.response

import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.rest.dto.meeting.MeetingDto

class MeetingsResponse(errorCode: ErrorCode, val meetings: Set<MeetingDto>) : BaseResponse(errorCode) {
}