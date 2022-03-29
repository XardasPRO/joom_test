package com.joom.calendar.calendar.rest.dto.meeting

import com.joom.calendar.calendar.rest.dto.user.UserDto

class MeetingMemberDto(
    val user: UserDto,
    val isConfirmed: Boolean,
    val isCanceled: Boolean
) {
    fun copy(
        user: UserDto = this.user,
        isConfirmed: Boolean = this.isConfirmed,
        isCanceled: Boolean = this.isCanceled
    ): MeetingMemberDto {
        return MeetingMemberDto(user, isConfirmed, isCanceled)
    }
}