package com.joom.calendar.calendar.rest.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "User create request")
class CreateUserRequest(
    @field:Schema(description = "User name")
    val name: String,
    @field:Schema(description = "User surname")
    val surname: String,
    @field:Schema(description = "User login")
    val login: String,
    @field:Schema(description = "User password")
    val password: String,
    @field:Schema(description = "User email")
    val email: String?,
    @field:Schema(description = "User timezone offset from UTC")
    val zoneOffset: String,
    @field:Schema(description = "User authorities")
    val authorities: Set<String>
) {
    fun copy(
        name: String = this.name,
        surname: String = this.surname,
        login: String = this.login,
        password: String = this.password,
        email: String? = this.email,
        zoneOffset: String = this.zoneOffset,
        authorities: Set<String> = this.authorities
    ): CreateUserRequest {
        return CreateUserRequest(name, surname, login, password, email, zoneOffset, authorities)
    }
}