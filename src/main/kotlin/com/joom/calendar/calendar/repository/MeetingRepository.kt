package com.joom.calendar.calendar.repository;

import com.joom.calendar.calendar.domain.meeting.Meeting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime
import java.util.*

@Repository
interface MeetingRepository : JpaRepository<Meeting, UUID> {

    @Query(
        """
            select me from Meeting me 
            left join MeetingMember mm on mm.meeting.id = me.id
            left join MeetingSchedule ms on ms.meeting.id = me.id
            where 
                (mm.user.id = :userId and mm.isCanceled = false) and
                ((ms.schedule.isRepeatable = false and ms.schedule.startDateTime >= :from and ms.schedule.startDateTime < :to)
                    or ms.schedule.isRepeatable = true )
        """
    )
    fun findAllByUserIdAndRange(userId: UUID, from: ZonedDateTime, to: ZonedDateTime): Set<Meeting>
}