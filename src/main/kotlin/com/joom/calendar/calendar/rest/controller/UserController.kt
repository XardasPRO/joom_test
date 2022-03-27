package com.joom.calendar.calendar.rest.controller

import com.joom.calendar.calendar.domain.service.UserService
import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.rest.dto.request.CreateUserRequest
import com.joom.calendar.calendar.rest.dto.request.UpdateUserScheduleRequest
import com.joom.calendar.calendar.rest.dto.response.BaseResponse
import com.joom.calendar.calendar.rest.dto.response.CreateUserResponse
import com.joom.calendar.calendar.rest.utils.EntityMapper
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
        val createdUser = userService.createUser(request)
        return CreateUserResponse(ErrorCode.OK, EntityMapper.userToDto(createdUser))
    }

    @PostMapping("/update_schedule")
    fun updateSchedule(@RequestBody request: UpdateUserScheduleRequest): BaseResponse {
        val updatedUser = userService.updateUserSchedule(request)
        return CreateUserResponse(ErrorCode.OK, EntityMapper.userToDto(updatedUser))
    }
}