package com.joom.calendar.calendar.domain.service

import com.joom.calendar.calendar.domain.meeting.Meeting
import com.joom.calendar.calendar.domain.meeting.MeetingMember
import com.joom.calendar.calendar.domain.meeting.MeetingSchedule
import com.joom.calendar.calendar.domain.validator.MeetingValidator
import com.joom.calendar.calendar.model.notification.Notification
import com.joom.calendar.calendar.model.schedule.Schedule
import com.joom.calendar.calendar.model.user.User
import com.joom.calendar.calendar.repository.MeetingMemberRepository
import com.joom.calendar.calendar.repository.MeetingRepository
import com.joom.calendar.calendar.repository.MeetingScheduleRepository
import com.joom.calendar.calendar.repository.ScheduleRepository
import com.joom.calendar.calendar.rest.dto.meeting.MeetingDto
import com.joom.calendar.calendar.rest.dto.request.CreateMeetingRequest
import com.joom.calendar.calendar.rest.dto.schedule.ScheduleDto
import com.joom.calendar.calendar.rest.utils.EntityMapper
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class MeetingService(
    private val contextService: ContextService,
    private val meetingRepository: MeetingRepository,
    private val meetingMemberRepository: MeetingMemberRepository,
    private val meetingScheduleRepository: MeetingScheduleRepository,
    private val scheduleRepository: ScheduleRepository,
    private val meetingValidator: MeetingValidator,
    private val notificationService: NotificationService
) {

    fun setMeetingMemberStatus(meetingId: UUID, isConfirmed: Boolean = false, isCanceled: Boolean = false) {
        val authorisedUser = contextService.getAuthorisedUser()
        val meetingMember = meetingValidator.validateSetMeetingMemberStatusRequest(meetingId, authorisedUser)
        meetingMemberRepository.save(meetingMember.copy(isConfirmed = isConfirmed, isCanceled = isCanceled))
    }

    fun get(meetingId: UUID): MeetingDto {
        val authorisedUser = contextService.getAuthorisedUser()
        val meeting = meetingValidator.validateGetMeetingRequest(meetingId, authorisedUser)
        return EntityMapper.meetingToDto(meeting, authorisedUser.zoneOffset, false)
    }

    fun create(request: CreateMeetingRequest): MeetingDto {
        val authorisedUser = contextService.getAuthorisedUser()
        val invitedUsers: Set<User> = meetingValidator.validateCreateMeetingRequest(request, authorisedUser)

        val meeting = meetingRepository.save(
            Meeting(
                isPrivate = request.isPrivate,
                name = request.name,
                description = request.description,
                owner = authorisedUser,
                schedule = emptySet(),
                members = emptySet()
            )
        )

        val schedule = saveMeetingSchedule(meeting, request.schedule, authorisedUser.zoneOffset)

        val members = saveMeetingMembers(meeting, invitedUsers)

        notifyUsers(invitedUsers, meeting)

        return EntityMapper.meetingToDto(
            meeting.copy(schedule = schedule, members = members),
            authorisedUser.zoneOffset
        )
    }

    private fun saveMeetingSchedule(
        meeting: Meeting,
        schedule: Set<ScheduleDto>,
        zoneOffset: String
    ): Set<Schedule> {
        val schedules = scheduleRepository.saveAll(schedule.map {
            EntityMapper.scheduleDtoToEntity(it, zoneOffset)
        })
        meetingScheduleRepository.saveAll(schedules.map { MeetingSchedule(meeting = meeting, schedule = it) })
        return schedules.toSet()
    }

    private fun saveMeetingMembers(meeting: Meeting, users: Set<User>): Set<MeetingMember> {
        val usersWithOwner = users.toMutableSet()
        usersWithOwner.add(meeting.owner)
        val meetingMembers = usersWithOwner.map {
            MeetingMember(
                user = it,
                meeting = meeting,
                isConfirmed = false,
                isCanceled = false,
            )
        }
        return meetingMemberRepository.saveAll(meetingMembers).toSet()
    }

    private fun notifyUsers(members: Set<User>, meeting: Meeting) {
        members.forEach {
            notificationService.notify(
                Notification(
                    it,
                    "You was invited to meeting ${meeting.name}",
                    LocalDateTime.now()
                )
            )
        }
    }
}