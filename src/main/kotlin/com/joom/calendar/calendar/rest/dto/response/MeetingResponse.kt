package com.joom.calendar.calendar.rest.dto.response

import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.rest.dto.meeting.MeetingDto

class MeetingResponse(errorCode: ErrorCode, val meeting: MeetingDto) : BaseResponse(errorCode) {
}