package com.joom.calendar.calendar.domain.validator

import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.exception.ValidateException
import com.joom.calendar.calendar.model.schedule.ScheduleType
import com.joom.calendar.calendar.repository.UserRepository
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
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream

internal class MeetingSearchValidatorTest {
    companion object {
        val userRepository = mockk<UserRepository>()
        val testee = MeetingSearchValidator(
            userRepository
        )

        val user_1 = TestObjectFactory.createUser()
        val user_2 = TestObjectFactory.createUser()
        val user_3 = TestObjectFactory.createUser()

        val validUserIdSet: Set<UUID> = setOf(user_1.id, user_2.id, user_3.id)
        val invalidUserIdSet: Set<UUID> = setOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        val invalidUserIdSetSecond: Set<UUID> = setOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())

        val emptyUserId = UUID.randomUUID()

        @JvmStatic
        fun exceptionValidateFindFirstAvailableIntervalRequest(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(invalidUserIdSet, 1000L, ErrorCode.USER_IS_NOT_FOUND),
                Arguments.of(invalidUserIdSetSecond, 1000L, ErrorCode.USERS_DO_NOT_HAVE_TIME_FOR_MEETING)
            )
        }

        @JvmStatic
        fun exceptionValidateFindUserMeetingsRequest(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    emptyUserId,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusHours(1),
                    ErrorCode.USER_IS_NOT_FOUND
                ),
                Arguments.of(
                    user_1.id,
                    LocalDateTime.now(),
                    LocalDateTime.now().minusHours(1),
                    ErrorCode.VALIDATION_ERROR_INVALID_VALUE
                )
            )
        }

    }

    @BeforeEach
    fun initMocks() {
        every { userRepository.findById(emptyUserId) } returns Optional.empty()
        every { userRepository.findById(user_1.id) } returns Optional.of(user_1)
        every { userRepository.findAllById(validUserIdSet) } returns listOf(user_1, user_2, user_3)
        every { userRepository.findAllById(invalidUserIdSet) } returns listOf(
            user_1.copy(
                schedule = setOf(
                    TestObjectFactory.createSchedule()
                )
            )
        )
        every { userRepository.findAllById(invalidUserIdSetSecond) } returns listOf(
            user_1.copy(
                schedule = setOf(
                    TestObjectFactory.createSchedule().copy(type = ScheduleType.WORKDAYS, duration = 10)
                )
            ),
            user_2, user_3
        )
    }

    @AfterEach
    fun clear() {
        clearAllMocks()
    }

    @ParameterizedTest
    @MethodSource("exceptionValidateFindFirstAvailableIntervalRequest")
    fun `validator should throw correct exceptions on bad FindFirstAvailableIntervalRequest`(
        userIds: Set<UUID>,
        meetingDelay: Long,
        expectedErrorCode: ErrorCode
    ) {
        val actualCode = Assertions.assertThrows(ValidateException::class.java) {
            testee.validateFindFirstAvailableIntervalRequest(userIds, meetingDelay)
        }.errorCode
        Assertions.assertEquals(expectedErrorCode, actualCode)
    }

    @Test
    fun `should pass correct FindFirstAvailableIntervalRequest`() {
        val users =
            testee.validateFindFirstAvailableIntervalRequest(validUserIdSet, 10)
        Assertions.assertEquals(3, users.size)
    }

    @ParameterizedTest
    @MethodSource("exceptionValidateFindUserMeetingsRequest")
    fun `validator should throw correct exceptions on bad FindUserMeetingsRequest`(
        userId: UUID,
        from: LocalDateTime,
        to: LocalDateTime,
        expectedErrorCode: ErrorCode
    ) {
        val actualCode = Assertions.assertThrows(ValidateException::class.java) {
            testee.validateFindUserMeetingsRequest(userId, from, to)
        }.errorCode
        Assertions.assertEquals(expectedErrorCode, actualCode)
    }

    @Test
    fun `should pass correct FindUserMeetingsRequest`() {
        testee.validateFindUserMeetingsRequest(user_1.id, LocalDateTime.now(), LocalDateTime.now().plusHours(1))
    }
}