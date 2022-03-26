package com.joom.calendar.calendar.domain.validator

import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.exception.ValidateException
import com.joom.calendar.calendar.repository.UserRepository
import com.joom.calendar.calendar.rest.dto.request.CreateUserRequest
import org.springframework.stereotype.Service

@Service
class UserValidator(
    private val userRepository: UserRepository
) {
    fun validateCreateUserRequest(request: CreateUserRequest) {
        if (
            request.name.isEmpty() || request.surname.isEmpty() || request.login.isEmpty() ||
            request.password.isEmpty() || request.authorities.isEmpty()
        ) {
            throw ValidateException("Empty required field at request $request", ErrorCode.VALIDATION_ERROR_EMPTY_FIELD)
        }

        if (request.timezone < -18 || request.timezone > 18) {
            throw ValidateException("Invalid timezone. Must be between -18 and 18.", ErrorCode.VALIDATION_ERROR_INVALID_VALUE)
        }

        if (userRepository.findUserByLogin(request.login).isPresent) {
            throw ValidateException("User with login ${request.login} already exist", ErrorCode.VALIDATION_ERROR_USED_VALUE)
        }

        if (request.email != null && userRepository.findUserByEmail(request.email).isPresent) {
            throw ValidateException("User with email ${request.email} already exist", ErrorCode.VALIDATION_ERROR_USED_VALUE)
        }
    }
}