package com.joom.calendar.calendar.domain.meeting

import com.joom.calendar.calendar.model.BaseEntity
import com.joom.calendar.calendar.model.schedule.Schedule
import java.util.*
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.OneToOne

@Entity
class MeetingSchedule(
    id: UUID = UUID.randomUUID(),
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    val meeting: Meeting,
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schedule_id")
    val schedule: Schedule
) : BaseEntity(id) {
    fun copy(
        id: UUID = this.id,
        meeting: Meeting = this.meeting,
        schedule: Schedule = this.schedule
    ): MeetingSchedule {
        return MeetingSchedule(id, meeting, schedule)
    }
}