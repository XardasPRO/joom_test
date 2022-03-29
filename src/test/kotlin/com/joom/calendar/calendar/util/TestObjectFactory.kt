package com.joom.calendar.calendar.util

import com.joom.calendar.calendar.domain.meeting.Meeting
import com.joom.calendar.calendar.domain.meeting.MeetingMember
import com.joom.calendar.calendar.model.schedule.Schedule
import com.joom.calendar.calendar.model.schedule.ScheduleType
import com.joom.calendar.calendar.model.user.User
import com.joom.calendar.calendar.model.user.UserAuthority
import com.joom.calendar.calendar.model.user.UserWorkingSchedule
import com.joom.calendar.calendar.rest.dto.request.CreateMeetingRequest
import com.joom.calendar.calendar.rest.dto.request.CreateUserRequest
import com.joom.calendar.calendar.rest.dto.request.UpdateUserScheduleRequest
import com.joom.calendar.calendar.rest.dto.schedule.ScheduleDto
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class TestObjectFactory {
    companion object {
        fun createUser(): User {
            val user = User(
                id = UUID.randomUUID(),
                name = "testUserName",
                surname = "testUserSurname",
                login = "testUserLogin",
                password = "testUserPassword",
                email = "test@user.mail",
                zoneOffset = "+00:00",
                isEnabled = true,
                authorities = emptySet(),
                schedule = emptySet()
            )
            return user.copy(authorities = setOf(createUserAuthority("testAuthority", user)))
        }

        fun createUserAuthority(authority: String, user: User): UserAuthority {
            return UserAuthority(
                id = UUID.randomUUID(),
                user = user,
                authority = authority
            )
        }

        fun createCreateUserRequest(): CreateUserRequest {
            return CreateUserRequest(
                name = "testUserName",
                surname = "testUserSurname",
                login = "testUserLogin",
                password = "testUserPassword",
                email = "test@user.mail",
                zoneOffset = "+00:00",
                authorities = setOf("testAuthority")
            )
        }

        fun createScheduleDto(): ScheduleDto {
            return ScheduleDto(
                type = "DATE",
                startDateTime = LocalDateTime.of(2020, 1, 1, 8, 0),
                duration = 3600,
                false
            )
        }

        fun createUpdateUserScheduleRequest(): UpdateUserScheduleRequest {
            return UpdateUserScheduleRequest(
                userId = UUID.randomUUID(),
                zoneOffset = "+00:00",
                schedule = setOf(
                    createScheduleDto().copy(
                        type = "WORKDAYS",
                        startDateTime = LocalDateTime.of(2020, 1, 1, 8, 0),
                        duration = 14400
                    ),
                    createScheduleDto().copy(
                        type = "WORKDAYS",
                        startDateTime = LocalDateTime.of(2020, 1, 1, 13, 0),
                        duration = 14400
                    )
                )
            )
        }

        fun createUserWorkingSchedule(user: User, schedule: Schedule): UserWorkingSchedule {
            return UserWorkingSchedule(
                user = user,
                schedule = schedule
            )
        }

        fun createSchedule(): Schedule {
            return Schedule(
                type = ScheduleType.DAILY,
                startDateTime = ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 13, 0), ZoneId.of("UTC")),
                duration = 14400,
                isRepeatable = true
            )
        }

        fun createCreateMeetingRequest(): CreateMeetingRequest {
            return CreateMeetingRequest(
                isPrivate = false,
                name = "Test meeting name",
                description = "Test meeting description",
                schedule = setOf(
                    createScheduleDto().copy(type = "MONDAY"),
                    createScheduleDto().copy(duration = 1400)
                ),
                members = setOf(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID()
                )
            )
        }

        fun createMeeting(): Meeting {
            return Meeting(
                id = UUID.randomUUID(),
                isPrivate = false,
                name = "test meeting",
                description = "test description",
                owner = createUser(),
                schedule = setOf(createSchedule()),
                members = setOf()
            )
        }

        fun createMeetingMember(meeting: Meeting): MeetingMember {
            return MeetingMember(
                id = UUID.randomUUID(),
                user = createUser(),
                meeting = meeting,
                isConfirmed = false,
                isCanceled = false
            )
        }
    }
}