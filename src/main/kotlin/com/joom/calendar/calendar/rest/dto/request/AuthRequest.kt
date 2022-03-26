package com.joom.calendar.calendar.rest.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "User authorisation request")
class AuthRequest(
    @field:Schema(description = "User login")
    val login: String,
    @field:Schema(description = "User email")
    val email: String?,
    @field:Schema(description = "User password")
    val password: String
) {
}