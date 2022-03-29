package com.joom.calendar.calendar.domain.validator

import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.exception.ValidateException
import com.joom.calendar.calendar.model.user.User
import com.joom.calendar.calendar.repository.MeetingRepository
import com.joom.calendar.calendar.repository.UserRepository
import com.joom.calendar.calendar.rest.dto.request.CreateMeetingRequest
import com.joom.calendar.calendar.rest.dto.schedule.ScheduleDto
import com.joom.calendar.calendar.util.TestObjectFactory
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.stream.Stream

class MeetingValidatorTest {
    companion object {
        val userRepository = mockk<UserRepository>()
        val meetingRepository = mockk<MeetingRepository>()
        val testee = MeetingValidator(userRepository, meetingRepository)

        val existingUser_1 = TestObjectFactory.createUser()
        val existingUser_2 = TestObjectFactory.createUser()
        val existingUser_3 = TestObjectFactory.createUser()
        val testCreateMeetingRequest = TestObjectFactory.createCreateMeetingRequest()
        val validTestCreateMeetingRequest = TestObjectFactory.createCreateMeetingRequest().copy(
            members = setOf(existingUser_1.id, existingUser_2.id, existingUser_3.id)
        )

        val meeting = TestObjectFactory.createMeeting()
        val meetingMember = TestObjectFactory.createMeetingMember(meeting)
        val meetingWithMembers = meeting.copy(
            id = UUID.randomUUID(),
            isPrivate = true,
            members = setOf(
                meetingMember.copy(user = existingUser_2),
                meetingMember.copy(user = existingUser_3)
            )
        )

        @JvmStatic
        fun exceptionCreateMeetingValidatorData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(testCreateMeetingRequest.copy(name = ""), ErrorCode.VALIDATION_ERROR_EMPTY_FIELD),
                Arguments.of(
                    testCreateMeetingRequest.copy(schedule = emptySet()),
                    ErrorCode.VALIDATION_ERROR_EMPTY_FIELD
                ),
                Arguments.of(
                    testCreateMeetingRequest.copy(schedule = createSetOfSchedule(100, 60)),
                    ErrorCode.VALIDATION_ERROR_INVALID_VALUE
                ),
                Arguments.of(
                    testCreateMeetingRequest.copy(schedule = createSetOfSchedule(10, 85000)),
                    ErrorCode.VALIDATION_ERROR_INVALID_VALUE
                ),
                Arguments.of(
                    testCreateMeetingRequest.copy(schedule = createSetOfSchedule(10, 85000)),
                    ErrorCode.VALIDATION_ERROR_INVALID_VALUE
                ),
                Arguments.of(testCreateMeetingRequest, ErrorCode.USER_IS_NOT_FOUND),
            )
        }

        fun createSetOfSchedule(count: Int, durationMultiplayer: Long): Set<ScheduleDto> {
            val set = mutableSetOf<ScheduleDto>()
            for (i in 0..count) {
                set.add(TestObjectFactory.createScheduleDto().copy(duration = durationMultiplayer * (i + 1)))
            }
            return set
        }

        @JvmStatic
        fun exceptionGetMeetingValidatorData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(meeting.id, ErrorCode.MEETING_IS_NOT_FOUND),
                Arguments.of(meetingWithMembers.id, ErrorCode.FORBIDDEN),
            )
        }

        @JvmStatic
        fun correctGetMeetingValidatorData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(meetingWithMembers.id, meetingWithMembers.owner),
                Arguments.of(meetingWithMembers.id, existingUser_2),
                Arguments.of(
                    meetingWithMembers.id,
                    existingUser_1.copy(
                        authorities = setOf(
                            TestObjectFactory.createUserAuthority(
                                "admin",
                                existingUser_1
                            )
                        )
                    )
                ),
            )
        }

        @JvmStatic
        fun exceptionSetMeetingMemberStatusValidatorData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(meeting.id, existingUser_1, ErrorCode.MEETING_IS_NOT_FOUND),
                Arguments.of(meetingWithMembers.id, existingUser_1, ErrorCode.FORBIDDEN)
            )
        }

        @JvmStatic
        fun correctSetMeetingMemberStatusValidatorData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(meetingWithMembers.id, existingUser_2),
                Arguments.of(meetingWithMembers.id, existingUser_3)
            )
        }
    }

    @BeforeEach
    fun initMocks() {
        every { userRepository.findAllById(testCreateMeetingRequest.members) } returns emptyList()
        every { userRepository.findAllById(validTestCreateMeetingRequest.members) } returns listOf(
            existingUser_1,
            existingUser_2,
            existingUser_3
        )

        every { meetingRepository.findById(meeting.id) } returns Optional.empty()
        every { meetingRepository.findById(meetingWithMembers.id) } returns Optional.of(meetingWithMembers)
    }

    @AfterEach
    fun clear() {
        clearAllMocks()
    }

    @ParameterizedTest
    @MethodSource("exceptionCreateMeetingValidatorData")
    fun `validator should throw correct exceptions on bad create meeting request`(
        testRequest: CreateMeetingRequest,
        expectedErrorCode: ErrorCode
    ) {
        val actualCode = Assertions.assertThrows(ValidateException::class.java) {
            testee.validateCreateMeetingRequest(testRequest, existingUser_1)
        }.errorCode
        Assertions.assertEquals(expectedErrorCode, actualCode)
    }

    @Test
    fun `validator should pass correct create meeting request`() {
        val users =
            testee.validateCreateMeetingRequest(validTestCreateMeetingRequest, existingUser_1)

        Assertions.assertTrue(users.containsAll(setOf(existingUser_1, existingUser_2, existingUser_3)))
    }


    @ParameterizedTest
    @MethodSource("exceptionGetMeetingValidatorData")
    fun `validator should throw correct exceptions on bad get meeting request`(
        meetingId: UUID,
        expectedErrorCode: ErrorCode
    ) {
        val actualCode = Assertions.assertThrows(ValidateException::class.java) {
            testee.validateGetMeetingRequest(meetingId, existingUser_1)
        }.errorCode
        Assertions.assertEquals(expectedErrorCode, actualCode)
    }

    @ParameterizedTest
    @MethodSource("correctGetMeetingValidatorData")
    fun `validator should pass on correct get meeting request`(
        meetingId: UUID,
        authorizedUser: User
    ) {
        val meeting = testee.validateGetMeetingRequest(meetingId, authorizedUser)
        Assertions.assertNotNull(meeting)
    }

    @ParameterizedTest
    @MethodSource("exceptionSetMeetingMemberStatusValidatorData")
    fun `validator should throw correct exceptions on bad set meeting member request`(
        meetingId: UUID,
        authorizedUser: User,
        expectedErrorCode: ErrorCode
    ) {
        val actualCode = Assertions.assertThrows(ValidateException::class.java) {
            testee.validateSetMeetingMemberStatusRequest(meetingId, authorizedUser)
        }.errorCode
        Assertions.assertEquals(expectedErrorCode, actualCode)
    }


    @ParameterizedTest
    @MethodSource("correctSetMeetingMemberStatusValidatorData")
    fun `validator should pass on correct set meeting member request`(
        meetingId: UUID,
        authorizedUser: User
    ) {
        val meetingMember = testee.validateSetMeetingMemberStatusRequest(meetingId, authorizedUser)
        Assertions.assertNotNull(meetingMember)
    }
}