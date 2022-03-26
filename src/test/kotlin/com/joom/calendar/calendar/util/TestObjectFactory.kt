package com.joom.calendar.calendar.util

import com.joom.calendar.calendar.model.user.User
import com.joom.calendar.calendar.model.user.UserAuthority
import com.joom.calendar.calendar.rest.dto.request.CreateUserRequest
import java.util.*

class TestObjectFactory {
    companion object {
        fun createUser(): User {
            val user = User(
                id = UUID.randomUUID(),
                name = "testUserName",
                surname = "testUserSurname",
                login = "testUserLogin",
                password = "testUserPassword",
                email = "test@user.mail",
                timezone = 0,
                isEnabled = true,
                authorities = emptySet()
            )
            return user.copy(authorities = setOf(createUserAuthority("testAuthority", user)))
        }

        fun createUserAuthority(authority: String, user: User): UserAuthority {
            return UserAuthority(
                id = UUID.randomUUID(),
                user = user,
                authority = authority
            )
        }

        fun createCreateUserRequest(): CreateUserRequest {
            return CreateUserRequest(
                name = "testUserName",
                surname = "testUserSurname",
                login = "testUserLogin",
                password = "testUserPassword",
                email = "test@user.mail",
                timezone = 0,
                authorities = setOf("testAuthority")
            )
        }
    }
}