package com.joom.calendar.calendar.domain.service

import com.joom.calendar.calendar.domain.security.JwtTokenUtils
import com.joom.calendar.calendar.domain.validator.UserValidator
import com.joom.calendar.calendar.model.exception.AuthorizeException
import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.schedule.ScheduleType
import com.joom.calendar.calendar.repository.ScheduleRepository
import com.joom.calendar.calendar.repository.UserAuthorityRepository
import com.joom.calendar.calendar.repository.UserRepository
import com.joom.calendar.calendar.repository.UserWorkingScheduleRepository
import com.joom.calendar.calendar.rest.dto.request.AuthRequest
import com.joom.calendar.calendar.util.TestObjectFactory
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
import javax.servlet.http.HttpServletResponse

internal class UserServiceTest {
    companion object {
        private val contextService = mockk<ContextService>()
        private val userValidator = mockk<UserValidator>()
        private val passwordEncoder = mockk<PasswordEncoder>()
        private val userRepository = mockk<UserRepository>()
        private val userAuthorityRepository = mockk<UserAuthorityRepository>()
        private val scheduleRepository = mockk<ScheduleRepository>()
        private val userWorkingScheduleRepository = mockk<UserWorkingScheduleRepository>()
        private val jwtTokenUtils = mockk<JwtTokenUtils>()
        private val httpServletResponse = mockk<HttpServletResponse>()

        val testee = UserService(
            contextService,
            userValidator,
            passwordEncoder,
            userRepository,
            userAuthorityRepository,
            scheduleRepository,
            userWorkingScheduleRepository,
            jwtTokenUtils
        )

        private val authorisedUser = TestObjectFactory.createUser()
        private val token =
            "eyJhbGciOiJIUzI1NiJ9.eyJsb2dpbiI6ImFkbWluIiwiZXhwaXJlQXQiOiIxNjQ4NTA2NDgwIn0.ArYWNLLldfFi45z8KUGlGpdQXvaHP9U9hZI1y9tdVS4"
    }

    @BeforeEach
    fun initMocks() {
        every { contextService.getAuthorisedUser() } returns authorisedUser

        every { userRepository.findUserByLogin("login") }.returns(Optional.of(authorisedUser))
        every { userRepository.findUserByLogin("loginNotFound") }.returns(Optional.empty())
        every { userRepository.save(authorisedUser.copy(zoneOffset = "+00:00")) } returnsArgument 0

        every { passwordEncoder.matches("password", authorisedUser.password) }.returns(true)
        every { passwordEncoder.matches("wrongPassword", authorisedUser.password) }.returns(false)
        every { passwordEncoder.encode("testUserPassword") } returns "encodedPassword"

        every { jwtTokenUtils.generateTokenForUser(authorisedUser) }.returns(token)

        every { httpServletResponse.addCookie(any()) }.answers { }
        every { httpServletResponse.contentType = "text/plain" }.answers { }

        every { userValidator.validateCreateUserRequest(any()) }.answers { }
        every { userValidator.validateUpdateUserScheduleRequest(any(), any()) }.answers { authorisedUser }

        every { userRepository.save(any()) } returnsArgument 0

        every { userAuthorityRepository.save(any()) } returnsArgument 0

        every { userWorkingScheduleRepository.delete(any()) } answers {}
        every { userWorkingScheduleRepository.save(any()) } returnsArgument 0
        every { userWorkingScheduleRepository.findAllByUser(any()) } returns setOf(
            TestObjectFactory.createUserWorkingSchedule(authorisedUser, TestObjectFactory.createSchedule()),
            TestObjectFactory.createUserWorkingSchedule(
                authorisedUser,
                TestObjectFactory.createSchedule().copy(type = ScheduleType.WEEKENDS)
            )
        )

        every { scheduleRepository.delete(any()) } answers {}
        every { scheduleRepository.save(any()) } returnsArgument 0
    }

    @AfterEach
    fun clear() {
        clearAllMocks()
    }

    @Test
    fun `should authorise user`() {
        testee.authorise(
            AuthRequest(
                login = "login",
                email = null,
                password = "password"
            ),
            httpServletResponse
        )
        verify(exactly = 1) { httpServletResponse.addCookie(any()) }
        verify(exactly = 1) { httpServletResponse.contentType = any() }
    }

    @Test
    fun `should throw exception if authorise user is not found`() {
        val errorCode = Assertions.assertThrows(AuthorizeException::class.java) {
            testee.authorise(
                AuthRequest(
                    login = "loginNotFound",
                    email = null,
                    password = "password"
                ),
                httpServletResponse
            )
        }.errorCode
        Assertions.assertEquals(ErrorCode.USER_IS_NOT_FOUND, errorCode)
    }

    @Test
    fun `should throw exception if user has wrong password`() {
        val errorCode = Assertions.assertThrows(AuthorizeException::class.java) {
            testee.authorise(
                AuthRequest(
                    login = "login",
                    email = null,
                    password = "wrongPassword"
                ),
                httpServletResponse
            )
        }.errorCode
        Assertions.assertEquals(ErrorCode.AUTH_WRONG_PASSWORD, errorCode)
    }

    @Test
    fun `should create user`() {
        val request = TestObjectFactory.createCreateUserRequest()
        val createdUser = testee.createUser(request)

        Assertions.assertEquals(request.name, createdUser.name)
        Assertions.assertEquals(request.surname, createdUser.surname)
        Assertions.assertEquals(request.login, createdUser.login)
        Assertions.assertEquals("encodedPassword", createdUser.password)
        Assertions.assertEquals(request.email, createdUser.email)
        Assertions.assertEquals(request.zoneOffset, createdUser.zoneOffset)
        Assertions.assertEquals(true, createdUser.isEnabled)
        Assertions.assertEquals(1, createdUser.authorities.size)
        Assertions.assertNotNull(createdUser.authorities.firstOrNull { it.authority == "testAuthority" })

        verify(exactly = 1) { userValidator.validateCreateUserRequest(request) }
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { passwordEncoder.encode(request.password) }
        verify(exactly = 1) { userAuthorityRepository.save(createdUser.authorities.first()) }
    }

    @Test
    fun `should update user schedule`() {
        val request = TestObjectFactory.createUpdateUserScheduleRequest()
        val updatedUser = testee.updateUserSchedule(request)

        Assertions.assertEquals(request.schedule.size, updatedUser.schedule.size)
        Assertions.assertEquals(request.zoneOffset, updatedUser.zoneOffset)

        verify(exactly = 1) { userValidator.validateUpdateUserScheduleRequest(request, authorisedUser) }
        verify(exactly = 1) { contextService.getAuthorisedUser() }
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { userWorkingScheduleRepository.findAllByUser(any()) }
        verify(exactly = 2) { userWorkingScheduleRepository.delete(any()) }
        verify(exactly = 2) { userWorkingScheduleRepository.save(any()) }
        verify(exactly = 2) { scheduleRepository.delete(any()) }
        verify(exactly = 2) { scheduleRepository.save(any()) }
    }
}