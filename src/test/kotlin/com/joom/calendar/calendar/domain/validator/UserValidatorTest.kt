package com.joom.calendar.calendar.domain.validator

import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.exception.ValidateException
import com.joom.calendar.calendar.repository.UserRepository
import com.joom.calendar.calendar.rest.dto.request.CreateUserRequest
import com.joom.calendar.calendar.util.TestObjectFactory
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

internal class UserValidatorTest() {
    private val userRepository = mockk<UserRepository>()

    private val testee = UserValidator(userRepository)

    @BeforeEach
    fun initMocks() {
        every { userRepository.findUserByLogin("existingLogin") }.returns(Optional.of(TestObjectFactory.createUser()))
        every { userRepository.findUserByLogin("emptyLogin") }.returns(Optional.empty())

        every { userRepository.findUserByEmail("existingEmail") }.returns(Optional.of(TestObjectFactory.createUser()))
        every { userRepository.findUserByEmail("emptyEmail") }.returns(Optional.empty())
    }

    @ParameterizedTest
    @MethodSource("exceptionValidatorData")
    fun `validator should throw correct exceptions`(testRequest: CreateUserRequest, expectedErrorCode: ErrorCode) {
        val actualCode = Assertions.assertThrows(ValidateException::class.java) {
            testee.validateCreateUserRequest(testRequest)
        }.errorCode
        Assertions.assertEquals(expectedErrorCode, actualCode)
    }

    @ParameterizedTest
    @MethodSource("correctValidatorData")
    fun `validator should pass correct requests`(testRequest: CreateUserRequest) {
        testee.validateCreateUserRequest(testRequest)
    }

    companion object {
        @JvmStatic
        fun exceptionValidatorData(): List<Arguments> {
            LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            val testRequest = TestObjectFactory.createCreateUserRequest()
            return listOf(
                Arguments.of(testRequest.copy(name = ""), ErrorCode.VALIDATION_ERROR_EMPTY_FIELD),
                Arguments.of(testRequest.copy(surname = ""), ErrorCode.VALIDATION_ERROR_EMPTY_FIELD),
                Arguments.of(testRequest.copy(login = ""), ErrorCode.VALIDATION_ERROR_EMPTY_FIELD),
                Arguments.of(testRequest.copy(password = ""), ErrorCode.VALIDATION_ERROR_EMPTY_FIELD),
                Arguments.of(testRequest.copy(timezone = -19), ErrorCode.VALIDATION_ERROR_INVALID_VALUE),
                Arguments.of(testRequest.copy(timezone = 19), ErrorCode.VALIDATION_ERROR_INVALID_VALUE),
                Arguments.of(testRequest.copy(authorities = emptySet()), ErrorCode.VALIDATION_ERROR_EMPTY_FIELD),
                Arguments.of(testRequest.copy(login = "existingLogin"), ErrorCode.VALIDATION_ERROR_USED_VALUE),
                Arguments.of(testRequest.copy(login = "emptyLogin", email = "existingEmail"), ErrorCode.VALIDATION_ERROR_USED_VALUE)
            )
        }

        @JvmStatic
        fun correctValidatorData(): List<Arguments> {
            val testRequest = TestObjectFactory.createCreateUserRequest().copy(login = "emptyLogin", email = null)
            return listOf(
                Arguments.of(testRequest),
                Arguments.of(testRequest.copy(email = "emptyEmail")),
                Arguments.of(testRequest.copy(timezone = -18)),
                Arguments.of(testRequest.copy(timezone = 18)),
            )
        }
    }
}