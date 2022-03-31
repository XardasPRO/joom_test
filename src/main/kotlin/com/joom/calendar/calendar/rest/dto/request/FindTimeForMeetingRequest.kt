package com.joom.calendar.calendar.rest.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Find time for meeting request")
class FindTimeForMeetingRequest(
    @field:Schema(description = "Meeting duration in seconds")
    val duration: Long,
    @field:Schema(description = "Uuid set of user id's which invited to this meeting")
    val members: Set<UUID>
) {
    fun copy(
        duration: Long = this.duration,
        members: Set<UUID> = this.members
    ): FindTimeForMeetingRequest {
        return FindTimeForMeetingRequest(duration, members)
    }
}