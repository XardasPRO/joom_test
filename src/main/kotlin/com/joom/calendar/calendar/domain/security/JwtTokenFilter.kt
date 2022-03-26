package com.joom.calendar.calendar.domain.security

import com.joom.calendar.calendar.model.exception.AuthorizeException
import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.security.UniversalAuthenticationToken
import com.joom.calendar.calendar.repository.UserRepository
import org.jboss.logging.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtTokenFilter(
    val jwtTokenUtils: JwtTokenUtils,
    val userRepository: UserRepository
) : OncePerRequestFilter() {
    val logger = Logger.getLogger(JwtTokenFilter::class.java)

    @Value("\${spring.security.cookie-name}")
    var authCookieName: String = "calendarAuthCookie"

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            if (request.cookies.isEmpty()) {
                throw AuthorizeException("COOKIES IS EMPTY", ErrorCode.AUTH_EMPTY_COOKIES)
            }

            val authCookie = request.cookies.first { it.name.equals(authCookieName) }
                ?: throw AuthorizeException("Auth cookie is not exist", ErrorCode.AUTH_COOKIE_IS_NOT_EXIST)

            val userLoginAndTokenLifeEndDate = jwtTokenUtils.extractUserLoginAndTokenLifeEndDate(authCookie.value)

            if (userLoginAndTokenLifeEndDate.second <= LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) {
                throw AuthorizeException("Token is expired.", ErrorCode.AUTH_TOKEN_IS_EXPIRED)
            }

            userRepository.findUserByLogin(userLoginAndTokenLifeEndDate.first).ifPresent {
                val auth = UniversalAuthenticationToken(
                    it.authorities.map { SimpleGrantedAuthority(it.authority) }.toMutableSet(), it
                )
                auth.isAuthenticated = true
                SecurityContextHolder.getContext().authentication = auth
            }
        } catch (e: Exception) {
            logger.info("Authorisation error", e)
        }

        filterChain.doFilter(request, response)
    }
}