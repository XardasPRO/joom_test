package com.joom.calendar.calendar.domain.validator

import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.exception.ValidateException
import com.joom.calendar.calendar.model.user.User
import com.joom.calendar.calendar.repository.UserRepository
import com.joom.calendar.calendar.rest.dto.request.CreateUserRequest
import com.joom.calendar.calendar.rest.dto.request.UpdateUserScheduleRequest
import com.joom.calendar.calendar.util.TestObjectFactory
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

internal class UserValidatorTest() {
    companion object {
        private val userRepository = mockk<UserRepository>()
        private val testee = UserValidator(userRepository)

        private val validUserId = UUID.randomUUID()
        private val emptyUserId = UUID.randomUUID()

        @JvmStatic
        fun exceptionCreateUserValidatorData(): List<Arguments> {
            LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            val testRequest = TestObjectFactory.createCreateUserRequest()
            return listOf(
                Arguments.of(testRequest.copy(name = ""), ErrorCode.VALIDATION_ERROR_EMPTY_FIELD),
                Arguments.of(testRequest.copy(surname = ""), ErrorCode.VALIDATION_ERROR_EMPTY_FIELD),
                Arguments.of(testRequest.copy(login = ""), ErrorCode.VALIDATION_ERROR_EMPTY_FIELD),
                Arguments.of(testRequest.copy(password = ""), ErrorCode.VALIDATION_ERROR_EMPTY_FIELD),
                Arguments.of(testRequest.copy(zoneOffset = ""), ErrorCode.VALIDATION_ERROR_INVALID_VALUE),
                Arguments.of(testRequest.copy(zoneOffset = "00:00"), ErrorCode.VALIDATION_ERROR_INVALID_VALUE),
                Arguments.of(testRequest.copy(zoneOffset = "asdg"), ErrorCode.VALIDATION_ERROR_INVALID_VALUE),
                Arguments.of(testRequest.copy(authorities = emptySet()), ErrorCode.VALIDATION_ERROR_EMPTY_FIELD),
                Arguments.of(testRequest.copy(login = "existingLogin"), ErrorCode.VALIDATION_ERROR_USED_VALUE),
                Arguments.of(
                    testRequest.copy(login = "emptyLogin", email = "existingEmail"),
                    ErrorCode.VALIDATION_ERROR_USED_VALUE
                )
            )
        }

        @JvmStatic
        fun correctCreateUserValidatorData(): List<Arguments> {
            val testRequest = TestObjectFactory.createCreateUserRequest().copy(login = "emptyLogin", email = null)
            return listOf(
                Arguments.of(testRequest),
                Arguments.of(testRequest.copy(email = "emptyEmail")),
                Arguments.of(testRequest.copy(zoneOffset = "+00:00")),
                Arguments.of(testRequest.copy(zoneOffset = "+03:00")),
            )
        }

        @JvmStatic
        fun exceptionUpdateUserScheduleValidatorData(): List<Arguments> {
            val testRequest = TestObjectFactory.createUpdateUserScheduleRequest()
            val authorizedUser = TestObjectFactory.createUser()
            return listOf(
                Arguments.of(
                    testRequest.copy(userId = emptyUserId), authorizedUser, ErrorCode.USER_IS_NOT_FOUND
                ),
                Arguments.of(
                    testRequest.copy(userId = validUserId), authorizedUser, ErrorCode.FORBIDDEN
                ),
                Arguments.of(
                    testRequest.copy(
                        userId = validUserId,
                        schedule = setOf(TestObjectFactory.createScheduleDto().copy(type = "INVALID_TYPE"))
                    ),
                    authorizedUser.copy(id = validUserId),
                    ErrorCode.VALIDATION_ERROR_INVALID_VALUE
                ),
                Arguments.of(
                    testRequest.copy(
                        userId = validUserId,
                        schedule = setOf(TestObjectFactory.createScheduleDto().copy(duration = Long.MAX_VALUE))
                    ),
                    authorizedUser.copy(id = validUserId),
                    ErrorCode.VALIDATION_ERROR_INVALID_VALUE
                ),
            )
        }

        @JvmStatic
        fun correctUpdateUserScheduleValidatorData(): List<Arguments> {
            val testRequest = TestObjectFactory.createUpdateUserScheduleRequest().copy(userId = validUserId)
            val authorizedUser = TestObjectFactory.createUser().copy(id = validUserId)
            return listOf(
                Arguments.of(testRequest, authorizedUser),
                Arguments.of(
                    testRequest,
                    authorizedUser.copy(
                        id = UUID.randomUUID(),
                        authorities = setOf(TestObjectFactory.createUserAuthority("admin", authorizedUser))
                    )
                )
            )
        }
    }

    @BeforeEach
    fun initMocks() {
        every { userRepository.findUserByLogin("existingLogin") }.returns(Optional.of(TestObjectFactory.createUser()))
        every { userRepository.findUserByLogin("emptyLogin") }.returns(Optional.empty())

        every { userRepository.findUserByEmail("existingEmail") }.returns(Optional.of(TestObjectFactory.createUser()))
        every { userRepository.findUserByEmail("emptyEmail") }.returns(Optional.empty())

        every { userRepository.findById(validUserId) }.returns(
            Optional.of(
                TestObjectFactory.createUser().copy(id = validUserId)
            )
        )
        every { userRepository.findById(emptyUserId) }.returns(Optional.empty())
    }

    @AfterEach
    fun clear() {
        clearAllMocks()
    }

    @ParameterizedTest
    @MethodSource("exceptionCreateUserValidatorData")
    fun `validator should throw correct exceptions on bad create user request`(
        testRequest: CreateUserRequest,
        expectedErrorCode: ErrorCode
    ) {
        val actualCode = Assertions.assertThrows(ValidateException::class.java) {
            testee.validateCreateUserRequest(testRequest)
        }.errorCode
        Assertions.assertEquals(expectedErrorCode, actualCode)
    }

    @ParameterizedTest
    @MethodSource("correctCreateUserValidatorData")
    fun `validator should pass correct create user request`(testRequest: CreateUserRequest) {
        testee.validateCreateUserRequest(testRequest)
    }

    @ParameterizedTest
    @MethodSource("exceptionUpdateUserScheduleValidatorData")
    fun `validator should throw correct exception on bad update user schedule request`(
        testRequest: UpdateUserScheduleRequest,
        authorizedUser: User,
        expectedErrorCode: ErrorCode
    ) {
        val actualCode = Assertions.assertThrows(ValidateException::class.java) {
            testee.validateUpdateUserScheduleRequest(testRequest, authorizedUser)
        }.errorCode
        Assertions.assertEquals(expectedErrorCode, actualCode)
    }

    @ParameterizedTest
    @MethodSource("correctUpdateUserScheduleValidatorData")
    fun `validator should pass correct update user schedule request`(
        testRequest: UpdateUserScheduleRequest,
        authorizedUser: User
    ) {
        val user = testee.validateUpdateUserScheduleRequest(testRequest, authorizedUser)
        Assertions.assertEquals(testRequest.userId, user.id)
    }
}