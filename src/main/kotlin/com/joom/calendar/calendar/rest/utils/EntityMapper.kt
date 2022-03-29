package com.joom.calendar.calendar.rest.utils

import com.joom.calendar.calendar.domain.meeting.Meeting
import com.joom.calendar.calendar.domain.meeting.MeetingMember
import com.joom.calendar.calendar.model.schedule.Schedule
import com.joom.calendar.calendar.model.schedule.ScheduleType
import com.joom.calendar.calendar.model.user.User
import com.joom.calendar.calendar.rest.dto.meeting.MeetingDto
import com.joom.calendar.calendar.rest.dto.meeting.MeetingMemberDto
import com.joom.calendar.calendar.rest.dto.schedule.ScheduleDto
import com.joom.calendar.calendar.rest.dto.user.UserDto
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class EntityMapper {
    companion object {
        fun userToDto(user: User): UserDto {
            return UserDto(
                id = user.id,
                login = user.login,
                name = user.name,
                surname = user.surname,
                zoneOffset = user.zoneOffset
            )
        }

        fun scheduleDtoToEntity(dto: ScheduleDto, zoneOffset: String): Schedule {
            return Schedule(
                id = UUID.randomUUID(),
                type = ScheduleType.valueOf(dto.type),
                startDateTime = dto.startDateTime.atZone(ZoneId.ofOffset("UTC", ZoneOffset.of(zoneOffset)))
                    .withZoneSameInstant(ZoneId.of("UTC")),
                duration = dto.duration,
                isRepeatable = dto.isRepeatable
            )
        }

        fun scheduleToDto(schedule: Schedule, zoneOffset: String): ScheduleDto {
            return ScheduleDto(
                type = schedule.type.name,
                startDateTime = zonedDateTimeToLocal(schedule.startDateTime, zoneOffset),
                duration = schedule.duration,
                isRepeatable = schedule.isRepeatable
            )
        }

        fun meetingToDto(meeting: Meeting, zoneOffset: String, isPrivateVersion: Boolean = false): MeetingDto {
            if (isPrivateVersion) {
                return MeetingDto(
                    id = meeting.id,
                    isPrivate = meeting.isPrivate,
                    schedule = meeting.schedule.map { scheduleToDto(it, zoneOffset) }.toSet()
                )
            } else {
                return MeetingDto(
                    id = meeting.id,
                    isPrivate = meeting.isPrivate,
                    name = meeting.name,
                    description = meeting.description,
                    owner = userToDto(meeting.owner),
                    schedule = meeting.schedule.map { scheduleToDto(it, zoneOffset) }.toSet(),
                    members = meeting.members.map { meetingMemberToDto(it) }.toSet()
                )
            }

        }

        fun meetingMemberToDto(meetingMember: MeetingMember): MeetingMemberDto {
            return MeetingMemberDto(
                user = userToDto(meetingMember.user),
                isConfirmed = meetingMember.isConfirmed,
                isCanceled = meetingMember.isCanceled
            )
        }

        fun zonedDateTimeToLocal(dateTime: ZonedDateTime, zoneOffset: String): LocalDateTime {
            return dateTime.withZoneSameInstant(
                ZoneId.ofOffset(
                    "UTC",
                    ZoneOffset.of(zoneOffset)
                )
            ).toLocalDateTime()
        }

    }
}