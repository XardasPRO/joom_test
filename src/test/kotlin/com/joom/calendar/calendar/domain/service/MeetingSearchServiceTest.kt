package com.joom.calendar.calendar.domain.service

import com.joom.calendar.calendar.domain.validator.MeetingSearchValidator
import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.schedule.ScheduleType
import com.joom.calendar.calendar.repository.MeetingRepository
import com.joom.calendar.calendar.repository.UserRepository
import com.joom.calendar.calendar.util.TestObjectFactory
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

internal class MeetingSearchServiceTest {
    companion object {
        val contextService = mockk<ContextService>()
        val userRepository = mockk<UserRepository>()
        val meetingRepository = mockk<MeetingRepository>()
        val meetingSearchValidator = mockk<MeetingSearchValidator>()
        val testee = MeetingSearchService(
            contextService,
            userRepository,
            meetingRepository,
            meetingSearchValidator
        )

        val user_1 = TestObjectFactory.createUser()
            .copy(
                zoneOffset = ZonedDateTime.now().offset.id,
                schedule = setOf(
                    TestObjectFactory.createSchedule().copy(
                        type = ScheduleType.WORKDAYS,
                        startDateTime = ZonedDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0),
                        duration = 14400
                    ),
                    TestObjectFactory.createSchedule().copy(
                        type = ScheduleType.WORKDAYS,
                        startDateTime = ZonedDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0),
                        duration = 14400
                    ),
                )
            )
        val user_2 = TestObjectFactory.createUser()
            .copy(
                zoneOffset = ZonedDateTime.now().offset.id,
                schedule = setOf(
                    TestObjectFactory.createSchedule().copy(
                        type = ScheduleType.WORKDAYS,
                        startDateTime = ZonedDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0),
                        duration = 14400
                    ),
                    TestObjectFactory.createSchedule().copy(
                        type = ScheduleType.WORKDAYS,
                        startDateTime = ZonedDateTime.now().withHour(16).withMinute(0).withSecond(0).withNano(0),
                        duration = 14400
                    ),
                )
            )
        val user_3 = TestObjectFactory.createUser()
            .copy(
                zoneOffset = ZonedDateTime.now().offset.id,
                schedule = setOf(
                    TestObjectFactory.createSchedule().copy(
                        type = ScheduleType.WORKDAYS,
                        startDateTime = ZonedDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0),
                        duration = 17000
                    )
                )
            )
        val templateMeeting = TestObjectFactory.createMeeting()
        val dailyMeeting = TestObjectFactory.createMeeting()
            .copy(
                schedule = setOf(
                    TestObjectFactory.createSchedule().copy(
                        type = ScheduleType.WORKDAYS,
                        startDateTime = ZonedDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0),
                        duration = 600
                    )
                ),
                members = setOf(
                    TestObjectFactory.createMeetingMember(templateMeeting).copy(user = user_1),
                    TestObjectFactory.createMeetingMember(templateMeeting).copy(user = user_2),
                    TestObjectFactory.createMeetingMember(templateMeeting).copy(user = user_3)
                )
            )
        val u12MondayMeeting = TestObjectFactory.createMeeting()
            .copy(
                schedule = setOf(
                    TestObjectFactory.createSchedule().copy(
                        type = ScheduleType.MONDAY,
                        startDateTime = ZonedDateTime.now().withHour(11).withMinute(0).withSecond(0).withNano(0),
                        duration = 600
                    )
                ),
                members = setOf(
                    TestObjectFactory.createMeetingMember(templateMeeting).copy(user = user_1),
                    TestObjectFactory.createMeetingMember(templateMeeting).copy(user = user_2)
                )
            )

        val usersIds = setOf<UUID>(user_1.id, user_2.id, user_3.id)
    }

    @BeforeEach
    fun initMocks() {
        every { meetingSearchValidator.validateFindUserMeetingsRequest(user_1.id, any(), any()) } answers {  }
        every { meetingSearchValidator.validateFindFirstAvailableIntervalRequest(usersIds, any()) } returns setOf(user_1, user_2, user_3)

        every { contextService.getAuthorisedUser() } returns user_1

        every { meetingRepository.findAllByUserIdAndRange(user_1.id, any(), any()) } returns setOf(dailyMeeting, u12MondayMeeting)

        every { meetingRepository.findAllByUserIdAndRange(user_1.id, any(), any()) } returns setOf(dailyMeeting, u12MondayMeeting)
        every { meetingRepository.findAllByUserIdAndRange(user_2.id, any(), any()) } returns setOf(dailyMeeting, u12MondayMeeting)
        every { meetingRepository.findAllByUserIdAndRange(user_3.id, any(), any()) } returns setOf(dailyMeeting)
    }

    @AfterEach
    fun clear() {
        clearAllMocks()
    }

    @Test
    fun `should find meetings`() {
        val meetings = testee.findUserMeetingsInRange(
            user_1.id,
            LocalDateTime.now().minusWeeks(1),
            LocalDateTime.now().plusWeeks(1)
        )
        Assertions.assertEquals(2, meetings.meetings.size)
        Assertions.assertEquals(ErrorCode.OK, meetings.errorCode)
    }

    @Test
    fun `should find available interval`() {
        val interval = testee.findFirstAvailableInterval(
            usersIds, 600
        )
    }

    //todo make test for all meeting conditions
}