package com.joom.calendar.calendar.rest.dto.request

import com.joom.calendar.calendar.rest.dto.schedule.ScheduleDto
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Create meeting request")
class CreateMeetingRequest(
    @field:Schema(description = "If true then this meeting details shows only for meeting members")
    val isPrivate: Boolean,
    @field:Schema(description = "Name of the meeting")
    val name: String,
    @field:Schema(description = "Meeting description")
    val description: String?,
    @field:Schema(description = "Combination of rules describes meeting schedule")
    val schedule: Set<ScheduleDto>,
    @field:Schema(description = "Uuid set of members which invited to this meeting")
    val members: Set<UUID>
) {
    fun copy(
        isPrivate: Boolean = this.isPrivate,
        name: String = this.name,
        description: String? = this.description,
        schedule: Set<ScheduleDto> = this.schedule,
        members: Set<UUID> = this.members
    ): CreateMeetingRequest {
        return CreateMeetingRequest(isPrivate, name, description, schedule, members)
    }
}