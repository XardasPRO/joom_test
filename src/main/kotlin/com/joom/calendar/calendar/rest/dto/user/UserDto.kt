package com.joom.calendar.calendar.rest.dto.user

import java.util.UUID

class UserDto(
    val id: UUID?,
    val login: String,
    val name: String?
) {
}