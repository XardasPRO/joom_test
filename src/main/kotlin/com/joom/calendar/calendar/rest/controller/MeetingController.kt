package com.joom.calendar.calendar.rest.controller

import com.joom.calendar.calendar.domain.service.MeetingSearchService
import com.joom.calendar.calendar.domain.service.MeetingService
import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.mapper.EntityMapper
import com.joom.calendar.calendar.rest.dto.request.CreateMeetingRequest
import com.joom.calendar.calendar.rest.dto.request.FindTimeForMeetingRequest
import com.joom.calendar.calendar.rest.dto.response.*
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/meeting")
class MeetingController(
    private val meetingService: MeetingService,
    private val meetingSearchService: MeetingSearchService
) {

    @PostMapping("/create")
    fun create(@RequestBody request: CreateMeetingRequest): BaseResponse {
        val createdMeeting = meetingService.create(request)
        return MeetingResponse(ErrorCode.OK, createdMeeting)
    }

    @GetMapping("/get")
    fun get(@RequestParam meetingId: UUID): BaseResponse {
        return MeetingResponse(ErrorCode.OK, meetingService.get(meetingId))
    }

    @GetMapping("/confirm")
    fun accept(@RequestParam meetingId: UUID): BaseResponse {
        meetingService.setMeetingMemberStatus(meetingId, isConfirmed = true)
        return MeetingResponse(ErrorCode.OK, meetingService.get(meetingId))
    }

    @GetMapping("/cancel")
    fun reject(@RequestParam meetingId: UUID): BaseResponse {
        meetingService.setMeetingMemberStatus(meetingId, isCanceled = true)
        return MeetingResponse(ErrorCode.OK, meetingService.get(meetingId))
    }

    @GetMapping("/user-schedule")
    fun getUserSchedule(
        @RequestParam userId: UUID,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime
    ): BaseResponse {
        return meetingSearchService.findUserMeetingsInRange(userId, from, to)
    }

    @PostMapping("/find-time")
    fun findTime(@RequestBody request: FindTimeForMeetingRequest): BaseResponse {
        val closestDateTime = meetingSearchService.findFirstAvailableInterval(request.members, request.duration)
        return TimeResponse(ErrorCode.OK, closestDateTime)
    }

}