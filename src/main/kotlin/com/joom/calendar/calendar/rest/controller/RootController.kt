package com.joom.calendar.calendar.rest.controller

import com.joom.calendar.calendar.domain.service.UserService
import com.joom.calendar.calendar.rest.dto.request.AuthRequest
import com.joom.calendar.calendar.rest.dto.user.UserDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/")
class RootController(
    val userService: UserService
) {
    @PostMapping("/login")
    fun login(@RequestBody request: AuthRequest, response: HttpServletResponse): ResponseEntity<UserDto> {
        return userService.authorise(request, response)
    }
}