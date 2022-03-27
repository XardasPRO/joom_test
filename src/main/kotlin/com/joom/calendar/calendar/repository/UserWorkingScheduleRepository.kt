package com.joom.calendar.calendar.repository

import com.joom.calendar.calendar.model.user.User
import com.joom.calendar.calendar.model.user.UserWorkingSchedule
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserWorkingScheduleRepository : JpaRepository<UserWorkingSchedule, UUID> {
    fun findAllByUser(user: User): Set<UserWorkingSchedule>
}