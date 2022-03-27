package com.joom.calendar.calendar.repository

import com.joom.calendar.calendar.model.schedule.Schedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ScheduleRepository: JpaRepository<Schedule, UUID> {
}