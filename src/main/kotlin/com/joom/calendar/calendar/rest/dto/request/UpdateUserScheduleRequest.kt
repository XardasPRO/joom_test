package com.joom.calendar.calendar.rest.dto.request

import com.joom.calendar.calendar.rest.dto.schedule.ScheduleDto
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Request with new user working schedule")
class UpdateUserScheduleRequest(
    @field:Schema(description = "If of updating user")
    val userId: UUID,
    @field:Schema(description = "User timezone offset from UTC.")
    val zoneOffset: String,
    @field:Schema(description = "Set of new schedule items which cower working time")
    val schedule: Set<ScheduleDto>
) {
    fun copy(
        userId: UUID = this.userId,
        zoneOffset: String = this.zoneOffset,
        schedule: Set<ScheduleDto> = this.schedule
    ): UpdateUserScheduleRequest {
        return UpdateUserScheduleRequest(
            userId, zoneOffset, schedule
        )
    }
}