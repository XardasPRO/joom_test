package com.joom.calendar.calendar.rest.controller

import com.joom.calendar.calendar.domain.service.UserService
import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.rest.dto.request.CreateUserRequest
import com.joom.calendar.calendar.rest.dto.response.BaseResponse
import com.joom.calendar.calendar.rest.dto.response.CreateUserResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/user")
class UserController(
    val userService: UserService
) {
    @PostMapping("/create")
    fun createUser(@RequestBody request: CreateUserRequest): BaseResponse {
        userService.createUser(request)
        return CreateUserResponse(ErrorCode.AUTH_EMPTY_COOKIES)
    }
}