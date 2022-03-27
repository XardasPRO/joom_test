package com.joom.calendar.calendar.model.user

import com.joom.calendar.calendar.model.BaseEntity
import com.joom.calendar.calendar.model.schedule.Schedule
import java.util.*
import javax.persistence.Entity
import javax.persistence.OneToOne

@Entity
class UserWorkingSchedule(
    id: UUID = UUID.randomUUID(),
    @OneToOne
    val user: User,
    @OneToOne
    val schedule: Schedule
) : BaseEntity(id) {
}