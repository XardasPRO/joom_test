package com.joom.calendar.calendar.domain.validator

import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.exception.ValidateException
import com.joom.calendar.calendar.model.user.User
import com.joom.calendar.calendar.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class MeetingSearchValidator(
    val userRepository: UserRepository
) {
    @Value("\${calendar.check-work-time-limit}")
    val isCheckWorkTimeLimit = true

    fun validateFindFirstAvailableIntervalRequest(userIds: Set<UUID>, meetingDelay: Long): Set<User> {
        val foundedUsers = userRepository.findAllById(userIds)
        if (foundedUsers.size != userIds.size) {
            val notFounded = userIds.filter { id -> foundedUsers.firstOrNull { user -> user.id == id } == null }
            throw ValidateException("Users with id's [$notFounded] not founded.", ErrorCode.USER_IS_NOT_FOUND)
        }

        if (isCheckWorkTimeLimit) {
            for (user in foundedUsers) {
                var isHaveInterval = false
                user.schedule.forEach { if (it.duration >= meetingDelay) isHaveInterval = true }
                if (user.schedule.isNotEmpty() && !isHaveInterval) {
                    throw ValidateException(
                        "User ${user.id} don't have working interval for meeting with duration $meetingDelay sec",
                        ErrorCode.USERS_DO_NOT_HAVE_TIME_FOR_MEETING
                    )
                }
            }
        }

        return foundedUsers.toSet()
    }

    fun validateFindUserMeetingsRequest(userId: UUID, from: LocalDateTime, to: LocalDateTime) {
        val user = userRepository.findById(userId)
        if (user.isEmpty) {
            throw ValidateException("User with id $userId is not found.", ErrorCode.USER_IS_NOT_FOUND)
        }
        if (to.isBefore(from)) {
            throw ValidateException("Search range is incorrect", ErrorCode.VALIDATION_ERROR_INVALID_VALUE)
        }
    }
}