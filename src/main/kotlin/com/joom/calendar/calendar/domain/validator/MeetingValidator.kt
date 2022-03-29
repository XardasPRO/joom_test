package com.joom.calendar.calendar.domain.validator

import com.joom.calendar.calendar.domain.meeting.Meeting
import com.joom.calendar.calendar.domain.meeting.MeetingMember
import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.exception.ValidateException
import com.joom.calendar.calendar.model.user.User
import com.joom.calendar.calendar.repository.MeetingRepository
import com.joom.calendar.calendar.repository.UserRepository
import com.joom.calendar.calendar.rest.dto.request.CreateMeetingRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class MeetingValidator(
    private val userRepository: UserRepository,
    private val meetingRepository: MeetingRepository
) {
    @Value("\${calendar.max-meeting-duration}")
    val maxMeetingDuration: Long = 84600

    @Value("\${calendar.meeting-schedule-items-limit}")
    val meetingScheduleItemsLimit: Long = 20

    fun validateSetMeetingMemberStatusRequest(meetingId: UUID, authorisedUser: User): MeetingMember {
        val meeting = meetingRepository.findById(meetingId)
        if (meeting.isEmpty) {
            throw ValidateException("Meeting with id $meetingId is not exist", ErrorCode.MEETING_IS_NOT_FOUND)
        }
        return meeting.get().members.find { it.user.id == authorisedUser.id }
            ?: throw ValidateException("You not invited to meeting with id $meetingId", ErrorCode.FORBIDDEN)
    }

    fun validateGetMeetingRequest(meetingId: UUID, authorisedUser: User): Meeting {
        val meeting = meetingRepository.findById(meetingId)
        if (meeting.isEmpty) {
            throw ValidateException("Meeting with id $meetingId is not exist", ErrorCode.MEETING_IS_NOT_FOUND)
        }
        val met = meeting.get()
        if (met.isPrivate && authorisedUser.authorities.find { it.authority == "admin" } == null &&
                met.owner.id != authorisedUser.id && met.members.find { it.user.id == authorisedUser.id } == null) {
            throw ValidateException("You don't have access to this meeting info", ErrorCode.FORBIDDEN)
        }
        return met
    }

    fun validateCreateMeetingRequest(request: CreateMeetingRequest, authorisedUser: User): Set<User> {
        if (request.name.isEmpty()) {
            throw ValidateException("Empty meeting name", ErrorCode.VALIDATION_ERROR_EMPTY_FIELD)
        }
        if (request.schedule.isEmpty()) {
            throw ValidateException("Meeting must have schedule", ErrorCode.VALIDATION_ERROR_EMPTY_FIELD)
        }
        if (request.schedule.size > meetingScheduleItemsLimit) {
            throw ValidateException(
                "Meeting schedule must have less than $meetingScheduleItemsLimit items",
                ErrorCode.VALIDATION_ERROR_INVALID_VALUE
            )
        }
        request.schedule.forEach {
            if (it.duration > maxMeetingDuration) {
                throw ValidateException(
                    "Meeting schedule duration must be less than $maxMeetingDuration",
                    ErrorCode.VALIDATION_ERROR_INVALID_VALUE
                )
            }
        }

        val foundedUsers = userRepository.findAllById(request.members)
        if (foundedUsers.size != request.members.size) {
            val notFounded = request.members.filter { id -> foundedUsers.firstOrNull { user -> user.id == id } == null }
            throw ValidateException("Users with id's [$notFounded] not founded.", ErrorCode.USER_IS_NOT_FOUND)
        }

        return foundedUsers.toSet()
    }
}