package com.joom.calendar.calendar.model.schedule

import com.joom.calendar.calendar.model.BaseEntity
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity
class Schedule(
    id: UUID = UUID.randomUUID(),
    @Enumerated(EnumType.STRING)
    val type: ScheduleType,
    val startDateTime: ZonedDateTime,
    val duration: Long,
    val isRepeatable: Boolean
) : BaseEntity(id) {
    fun copy(
        id: UUID = this.id,
        type: ScheduleType = this.type,
        startDateTime: ZonedDateTime = this.startDateTime,
        duration: Long = this.duration,
        isRepeatable: Boolean = this.isRepeatable
    ): Schedule {
        return Schedule(id, type, startDateTime, duration, isRepeatable)
    }
}