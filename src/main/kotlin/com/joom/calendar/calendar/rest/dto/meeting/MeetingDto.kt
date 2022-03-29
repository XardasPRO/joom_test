package com.joom.calendar.calendar.rest.dto.meeting

import com.joom.calendar.calendar.rest.dto.schedule.ScheduleDto
import com.joom.calendar.calendar.rest.dto.user.UserDto
import java.util.*

class MeetingDto(
    val id: UUID,
    val isPrivate: Boolean,
    val name: String? = null,
    val description: String? = null,
    val owner: UserDto? = null,
    val schedule: Set<ScheduleDto>,
    val members: Set<MeetingMemberDto>? = null
) {
    fun copy(
        id: UUID = this.id,
        isPrivate: Boolean = this.isPrivate,
        name: String? = this.name,
        description: String? = this.description,
        owner: UserDto? = this.owner,
        schedule: Set<ScheduleDto> = this.schedule,
        members: Set<MeetingMemberDto>? = this.members
    ): MeetingDto {
        return MeetingDto(id, isPrivate, name, description, owner, schedule, members)
    }
}