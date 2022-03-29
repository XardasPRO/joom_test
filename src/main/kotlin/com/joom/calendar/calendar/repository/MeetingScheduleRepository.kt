package com.joom.calendar.calendar.repository;

import com.joom.calendar.calendar.domain.meeting.MeetingSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*
@Repository
interface MeetingScheduleRepository : JpaRepository<MeetingSchedule, UUID> {
}