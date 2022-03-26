package com.joom.calendar.calendar.domain.service

import com.joom.calendar.calendar.domain.security.JwtTokenUtils
import com.joom.calendar.calendar.domain.validator.UserValidator
import com.joom.calendar.calendar.model.exception.AuthorizeException
import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.user.User
import com.joom.calendar.calendar.model.user.UserAuthority
import com.joom.calendar.calendar.repository.UserAuthorityRepository
import com.joom.calendar.calendar.repository.UserRepository
import com.joom.calendar.calendar.rest.dto.request.AuthRequest
import com.joom.calendar.calendar.rest.dto.request.CreateUserRequest
import com.joom.calendar.calendar.rest.dto.user.UserDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@Service
class UserService(
    private val userValidator: UserValidator,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val userAuthorityRepository: UserAuthorityRepository,
    private val jwtTokenUtils: JwtTokenUtils
) {

    @Value("\${spring.security.cookie-name}")
    private var authCookieName: String = "calendarAuthCookie"

    @Value("\${spring.security.token-lifetime}")
    private var cookieLifetime: Int = 86400

    fun authorise(request: AuthRequest, response: HttpServletResponse): ResponseEntity<UserDto> {
        var result = ResponseEntity.notFound().build<UserDto>()
        userRepository.findUserByLogin(request.login).ifPresentOrElse( { user ->
            if (passwordEncoder.matches(request.password, user.password)) {
                val token = jwtTokenUtils.generateTokenForUser(user)
                val cookie = Cookie(authCookieName, token)
                cookie.path = "/"
                cookie.maxAge = cookieLifetime
                response.addCookie(cookie)
                response.contentType = "text/plain"
                result = ResponseEntity.ok().body(UserDto(user.id, user.login, user.name))
            } else {
                throw AuthorizeException("Wrong password", ErrorCode.AUTH_WRONG_PASSWORD)
            }
        }, {
            throw AuthorizeException("User with login ${request.login} is not exist", ErrorCode.USER_IS_NOT_FOUND)
        })
        return result
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
                timezone = request.timezone,
                isEnabled = true,
                authorities = emptySet()
            )
        )
        for (authority in request.authorities) {
            userAuthorityRepository.save(UserAuthority(user = user, authority = authority))
        }
        return user
    }
}