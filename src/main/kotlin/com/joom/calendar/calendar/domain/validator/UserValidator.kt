package com.joom.calendar.calendar.domain.validator

import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.exception.ValidateException
import com.joom.calendar.calendar.model.schedule.ScheduleType
import com.joom.calendar.calendar.model.security.UniversalAuthenticationToken
import com.joom.calendar.calendar.model.user.User
import com.joom.calendar.calendar.repository.UserRepository
import com.joom.calendar.calendar.rest.dto.request.CreateUserRequest
import com.joom.calendar.calendar.rest.dto.request.UpdateUserScheduleRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.DateTimeException
import java.time.ZoneOffset

@Service
class UserValidator(
    private val userRepository: UserRepository
) {
    @Value("\${calendar.max-working-period-duration}")
    val maxWorkingPeriodDuration: Long = 84600

    fun validateCreateUserRequest(request: CreateUserRequest) {
        if (
            request.name.isEmpty() || request.surname.isEmpty() || request.login.isEmpty() ||
            request.password.isEmpty() || request.authorities.isEmpty()
        ) {
            throw ValidateException("Empty required field at request $request", ErrorCode.VALIDATION_ERROR_EMPTY_FIELD)
        }

        try {
            ZoneOffset.of(request.zoneOffset)
        } catch (e: DateTimeException) {
            throw ValidateException(e.message, ErrorCode.VALIDATION_ERROR_INVALID_VALUE)
        }

        if (userRepository.findUserByLogin(request.login).isPresent) {
            throw ValidateException(
                "User with login ${request.login} already exist",
                ErrorCode.VALIDATION_ERROR_USED_VALUE
            )
        }

        if (request.email != null && userRepository.findUserByEmail(request.email).isPresent) {
            throw ValidateException(
                "User with email ${request.email} already exist",
                ErrorCode.VALIDATION_ERROR_USED_VALUE
            )
        }
    }

    fun validateUpdateUserScheduleRequest(request: UpdateUserScheduleRequest, authorizedUser: User): User {
        val user = userRepository.findById(request.userId)
        if (user.isEmpty) {
            throw ValidateException("User with id ${request.userId} is not found.", ErrorCode.USER_IS_NOT_FOUND)
        }

        if (authorizedUser.id != user.get().id && authorizedUser.authorities.firstOrNull { it.authority == "admin" } == null) {
            throw ValidateException(
                "User ${authorizedUser.id} can't edit schedule for ${user.get().id}",
                ErrorCode.FORBIDDEN
            )
        }

        request.schedule.forEach {
            try {
                ScheduleType.valueOf(it.type)
            } catch (e: IllegalArgumentException) {
                throw ValidateException(e.message, ErrorCode.VALIDATION_ERROR_INVALID_VALUE)
            }

            if (it.duration > maxWorkingPeriodDuration) {
                throw ValidateException(
                    "Max working period duration must be less than $maxWorkingPeriodDuration",
                    ErrorCode.VALIDATION_ERROR_INVALID_VALUE
                )
            }
        }

        return user.get()
    }
}