package com.joom.calendar.calendar.domain.service

import com.joom.calendar.calendar.domain.meeting.MeetingMember
import com.joom.calendar.calendar.domain.meeting.MeetingSchedule
import com.joom.calendar.calendar.domain.validator.MeetingValidator
import com.joom.calendar.calendar.model.schedule.Schedule
import com.joom.calendar.calendar.repository.MeetingMemberRepository
import com.joom.calendar.calendar.repository.MeetingRepository
import com.joom.calendar.calendar.repository.MeetingScheduleRepository
import com.joom.calendar.calendar.repository.ScheduleRepository
import com.joom.calendar.calendar.util.TestObjectFactory
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

internal class MeetingServiceTest {
    companion object {
        val contextService = mockk<ContextService>()
        val meetingRepository = mockk<MeetingRepository>()
        val meetingMemberRepository = mockk<MeetingMemberRepository>()
        val meetingScheduleRepository = mockk<MeetingScheduleRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val meetingValidator = mockk<MeetingValidator>()
        val notificationService = mockk<NotificationService>()

        val testee = MeetingService(
            contextService,
            meetingRepository,
            meetingMemberRepository,
            meetingScheduleRepository,
            scheduleRepository,
            meetingValidator,
            notificationService
        )

        val createMeetingRequest = TestObjectFactory.createCreateMeetingRequest()
        val authenticatedUser = TestObjectFactory.createUser().copy(zoneOffset = "+02:00")
        val meetingMembers = setOf(
            TestObjectFactory.createUser().copy(name = "member_1", zoneOffset = "+00:00"),
            TestObjectFactory.createUser().copy(name = "member_2", zoneOffset = "+01:00"),
            TestObjectFactory.createUser().copy(name = "member_3", zoneOffset = "+03:00")
        )

        val meeting = TestObjectFactory.createMeeting().copy(owner = authenticatedUser)
        val meetingWithMembers =
            meeting.copy(members = setOf(TestObjectFactory.createMeetingMember(meeting).copy(user = authenticatedUser)))
    }

    @BeforeEach
    fun initMocks() {
        every { contextService.getAuthorisedUser() } returns authenticatedUser

        every {
            meetingValidator.validateCreateMeetingRequest(
                createMeetingRequest,
                authenticatedUser
            )
        } returns meetingMembers
        every { meetingValidator.validateGetMeetingRequest(meeting.id, authenticatedUser) } returns meeting
        every {
            meetingValidator.validateSetMeetingMemberStatusRequest(
                meetingWithMembers.id,
                authenticatedUser
            )
        } returns meetingWithMembers.members.first()

        every { meetingRepository.save(any()) } returnsArgument 0
        every { meetingRepository.findById(meeting.id) } returns Optional.of(meeting)

        every { scheduleRepository.saveAll(any<List<Schedule>>()) } returnsArgument 0

        every { meetingScheduleRepository.saveAll(any<List<MeetingSchedule>>()) } returnsArgument 0

        every { meetingMemberRepository.saveAll(any<List<MeetingMember>>()) } returnsArgument 0
        every { meetingMemberRepository.save(any()) } returnsArgument 0

        every { notificationService.notify(any()) } answers {}

    }


    @AfterEach
    fun clear() {
        clearAllMocks()
    }

    @Test
    fun `should create meeting`() {
        val createdMeeting = testee.create(createMeetingRequest)
        Assertions.assertEquals(createMeetingRequest.name, createdMeeting.name)
        Assertions.assertEquals(createMeetingRequest.description, createdMeeting.description)
        Assertions.assertEquals(createMeetingRequest.isPrivate, createdMeeting.isPrivate)
        Assertions.assertEquals(createMeetingRequest.schedule.size, createdMeeting.schedule.size)
        Assertions.assertEquals(createMeetingRequest.members.size + 1, createdMeeting.members?.size)

        verify(exactly = 3) { notificationService.notify(any()) }
    }

    @Test
    fun `should return meeting by get request`() {
        val meetingDto = testee.get(meeting.id)
        Assertions.assertEquals(meeting.id, meetingDto.id)
        Assertions.assertEquals(meeting.name, meetingDto.name)
        Assertions.assertEquals(meeting.description, meetingDto.description)
        Assertions.assertEquals(meeting.isPrivate, meetingDto.isPrivate)
        Assertions.assertEquals(meeting.schedule.size, meetingDto.schedule.size)
        Assertions.assertEquals(meeting.members.size, meetingDto.members?.size)
    }

    @Test
    fun `should change meeting member status`() {
        testee.setMeetingMemberStatus(meetingWithMembers.id, isConfirmed = true)
        verify(exactly = 1) { meetingMemberRepository.save(any()) }
    }
}