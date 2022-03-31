package com.joom.calendar.calendar.domain.service

import com.joom.calendar.calendar.domain.security.JwtTokenUtils
import com.joom.calendar.calendar.domain.validator.UserValidator
import com.joom.calendar.calendar.model.exception.AuthorizeException
import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.mapper.EntityMapper
import com.joom.calendar.calendar.model.schedule.Schedule
import com.joom.calendar.calendar.model.user.User
import com.joom.calendar.calendar.model.user.UserAuthority
import com.joom.calendar.calendar.model.user.UserWorkingSchedule
import com.joom.calendar.calendar.repository.ScheduleRepository
import com.joom.calendar.calendar.repository.UserAuthorityRepository
import com.joom.calendar.calendar.repository.UserRepository
import com.joom.calendar.calendar.repository.UserWorkingScheduleRepository
import com.joom.calendar.calendar.rest.dto.request.AuthRequest
import com.joom.calendar.calendar.rest.dto.request.CreateUserRequest
import com.joom.calendar.calendar.rest.dto.request.UpdateUserScheduleRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@Service
class UserService(
    private val contextService: ContextService,
    private val userValidator: UserValidator,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val userAuthorityRepository: UserAuthorityRepository,
    private val scheduleRepository: ScheduleRepository,
    private val userWorkingScheduleRepository: UserWorkingScheduleRepository,
    private val jwtTokenUtils: JwtTokenUtils
) {

    @Value("\${spring.security.cookie-name}")
    private var authCookieName: String = "calendarAuthCookie"

    @Value("\${spring.security.token-lifetime}")
    private var cookieLifetime: Int = 86400

    fun authorise(request: AuthRequest, response: HttpServletResponse) {
        userRepository.findUserByLogin(request.login).ifPresentOrElse({ user ->
            if (passwordEncoder.matches(request.password, user.password)) {
                val token = jwtTokenUtils.generateTokenForUser(user)
                val cookie = Cookie(authCookieName, token)
                cookie.path = "/"
                cookie.maxAge = cookieLifetime
                response.addCookie(cookie)
                response.contentType = "text/plain"
            } else {
                throw AuthorizeException("Wrong password", ErrorCode.AUTH_WRONG_PASSWORD)
            }
        }, {
            throw AuthorizeException("User with login ${request.login} is not exist", ErrorCode.USER_IS_NOT_FOUND)
        })
    }

    @Transactional
    fun createUser(request: CreateUserRequest): User {
        userValidator.validateCreateUserRequest(request)
        val user = userRepository.save(
            User(
                name = request.name,
                surname = request.surname,
                login = request.login,
                password = passwordEncoder.encode(request.password),
                email = request.email,
                zoneOffset = request.zoneOffset,
                isEnabled = true,
                authorities = emptySet()
            )
        )
        val userAuthorities = mutableSetOf<UserAuthority>()
        for (authority in request.authorities) {
            userAuthorities.add(userAuthorityRepository.save(UserAuthority(user = user, authority = authority)))
        }

        return user.copy(authorities = userAuthorities)
    }

    @Transactional
    fun updateUserSchedule(request: UpdateUserScheduleRequest): User {
        var user = userValidator.validateUpdateUserScheduleRequest(
            request, contextService.getAuthorisedUser()
        )
        //update user zone offset
        user = userRepository.save(user.copy(zoneOffset = request.zoneOffset))
        //delete old schedule
        userWorkingScheduleRepository.findAllByUser(user).forEach {
            userWorkingScheduleRepository.delete(it)
            scheduleRepository.delete(it.schedule)
        }
        //create new schedule
        val userSchedule = mutableSetOf<Schedule>()
        request.schedule.forEach {
            val schedule = EntityMapper.scheduleDtoToEntity(it, user.zoneOffset)
            val savedSchedule = scheduleRepository.save(schedule)
            userSchedule.add(savedSchedule)
            userWorkingScheduleRepository.save(UserWorkingSchedule(user = user, schedule = savedSchedule))
        }

        return user.copy(schedule = userSchedule)
    }
}