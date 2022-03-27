package com.joom.calendar.calendar.rest.dto.schedule

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Schedule model")
class ScheduleDto(
    @field:Schema(description = "Type of schedule")
    val type: String,
    @field:Schema(description = "Date and time of schedule start. Applies at user timezone.")
    val startDateTime: LocalDateTime,
    @field:Schema(description = "Duration of schedule after start. Dimension is seconds")
    val duration: Long,
    @field:Schema(description = "Flag of repeat schedule. Applicable only for DATE and week days schedule type's.")
    val isRepeatable: Boolean
) {
    fun copy(
        type: String = this.type,
        startDateTime: LocalDateTime = this.startDateTime,
        duration: Long = this.duration,
        isRepeatable: Boolean = this.isRepeatable
    ): ScheduleDto {
        return ScheduleDto(type, startDateTime, duration, isRepeatable)
    }
}