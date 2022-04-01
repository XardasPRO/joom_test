package com.joom.calendar.calendar.domain.service

import com.joom.calendar.calendar.domain.meeting.Meeting
import com.joom.calendar.calendar.domain.validator.MeetingSearchValidator
import com.joom.calendar.calendar.model.exception.BusinessException
import com.joom.calendar.calendar.model.exception.ErrorCode
import com.joom.calendar.calendar.model.mapper.EntityMapper
import com.joom.calendar.calendar.model.schedule.Schedule
import com.joom.calendar.calendar.model.schedule.ScheduleType
import com.joom.calendar.calendar.repository.MeetingRepository
import com.joom.calendar.calendar.repository.UserRepository
import com.joom.calendar.calendar.rest.dto.response.MeetingsResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.*
import java.util.*

@Service
class MeetingSearchService(
    private val contextService: ContextService,
    private val userRepository: UserRepository,
    private val meetingRepository: MeetingRepository,
    private val meetingSearchValidator: MeetingSearchValidator
) {

    @Value("\${calendar.search-limit-days}")
    val searchLimitDays = 30L

    fun findFirstAvailableInterval(userIds: Set<UUID>, meetingDelay: Long): ZonedDateTime {
        val authorisedUser = contextService.getAuthorisedUser()
        val users = meetingSearchValidator.validateFindFirstAvailableIntervalRequest(userIds, meetingDelay)

        val startSearch = ZonedDateTime.now()
        val endSearch = startSearch.plusDays(searchLimitDays)

        var checkingTime = startSearch
        val usersMeetings =
            users.associateWith { meetingRepository.findAllByUserIdAndRange(it.id, startSearch, endSearch) }
        while (checkingTime.isBefore(endSearch)) {
            //1 check user working time
            var isUsersHaveWorkingTimeAtPeriod = true
            var isUsersHaveFreeTimeAtPeriod = true
            for (user in users) {
                val workingPeriod = user.schedule.filter {
                    isScheduleCrossPeriod(
                        it,
                        Pair(checkingTime, checkingTime.plusSeconds(meetingDelay)),
                        user.zoneOffset
                    )
                }
                if (workingPeriod.isEmpty()) {
                    //search next working period for this user
                    val closestWorkingTime = findClosestWorkingTime(user.schedule, checkingTime, endSearch)
                        ?: throw BusinessException(
                            "Crossing working time at period is not found.",
                            ErrorCode.CROSSING_WORK_TIME_NOT_FOUND
                        )
                    checkingTime = closestWorkingTime
                    isUsersHaveWorkingTimeAtPeriod = false
                    break
                }

                //2 check is have free time
                if (isUsersHaveWorkingTimeAtPeriod) {
                    for (meeting in usersMeetings.getOrDefault(user, emptySet())) {
                        for (schedule in meeting.schedule) {
                            val scheduleCrossPeriod = isScheduleCrossPeriod(
                                schedule,
                                Pair(checkingTime, checkingTime.plusSeconds(meetingDelay)),
                                user.zoneOffset
                            )
                            if (scheduleCrossPeriod) { //conflict with meeting
                                val meetingStart =
                                    convertZonedDateTimeToOffset(schedule.startDateTime, checkingTime.offset.id)
                                val newCheckingTime = checkingTime
                                    .withHour(meetingStart.hour)
                                    .withMinute(meetingStart.minute)
                                    .withSecond(meetingStart.second)
                                    .withNano(0)
                                    .plusSeconds(schedule.duration)
                                checkingTime =
                                    if (checkingTime.withNano(0) == newCheckingTime || newCheckingTime.isBefore(
                                            checkingTime
                                        )
                                    ) {
                                        newCheckingTime.plusDays(1)
                                    } else {
                                        newCheckingTime
                                    }
                                isUsersHaveFreeTimeAtPeriod = false
                                break
                            }
                        }
                    }
                }
            }
            if (isUsersHaveWorkingTimeAtPeriod && isUsersHaveFreeTimeAtPeriod) {
                return convertZonedDateTimeToOffset(checkingTime, authorisedUser.zoneOffset)
            }
        }
        throw BusinessException(
            "Users don't have a free time for this meeting",
            ErrorCode.USERS_DO_NOT_HAVE_TIME_FOR_MEETING
        )
    }

    //todo should be optimized
    private fun findClosestWorkingTime(
        schedule: Set<Schedule>,
        startTime: ZonedDateTime,
        timeLimit: ZonedDateTime
    ): ZonedDateTime? {
        var checkedTime = startTime
        while (checkedTime.isBefore(timeLimit)) {
            val checkingTimeEnd = checkedTime.plusMinutes(1)
            val workingPeriod = schedule.filter {
                isScheduleCrossPeriod(
                    it,
                    Pair(checkedTime, checkingTimeEnd),
                    "+00:00" //?
                )
            }
            if (workingPeriod.isNotEmpty()) {
                return checkingTimeEnd
            }
            checkedTime = checkingTimeEnd
        }
        return null
    }


    fun findUserMeetingsInRange(userId: UUID, from: LocalDateTime, to: LocalDateTime): MeetingsResponse {
        meetingSearchValidator.validateFindUserMeetingsRequest(userId, from, to)
        val authorisedUser = contextService.getAuthorisedUser()
        val searchFromUTC = convertDateTimeToUTC(from, authorisedUser.zoneOffset)
        val searchToUTC = convertDateTimeToUTC(to, authorisedUser.zoneOffset)

        val userMeetings = meetingRepository.findAllByUserIdAndRange(userId, searchFromUTC, searchToUTC)
        val meetingsInPeriod =
            userMeetings.filter { isMeetingInThePeriod(it, Pair(searchFromUTC, searchToUTC)) }.toSet()

        val isShowPrivateMeetings = authorisedUser.authorities.find { it.authority == "admin" } != null
                || (authorisedUser.id == userId)

        return MeetingsResponse(
            ErrorCode.OK,
            meetingsInPeriod.map {
                EntityMapper.meetingToDto(
                    it,
                    authorisedUser.zoneOffset,
                    it.isPrivate && !isShowPrivateMeetings
                )
            }.toSet()
        )
    }

    private fun convertDateTimeToUTC(localDateTime: LocalDateTime, zoneOffset: String): ZonedDateTime {
        return localDateTime.atZone(ZoneId.ofOffset("UTC", ZoneOffset.of(zoneOffset)))
            .withZoneSameInstant(ZoneId.of("UTC"))
    }

    private fun convertZonedDateTimeToOffset(dateTime: ZonedDateTime, zoneOffset: String): ZonedDateTime {
        return dateTime.withZoneSameInstant(ZoneId.ofOffset("UTC", ZoneOffset.of(zoneOffset)))
    }

    private fun isMeetingInThePeriod(meeting: Meeting, period: Pair<ZonedDateTime, ZonedDateTime>): Boolean {
        val ownerOffset = meeting.owner.zoneOffset
        val fromToPeriod = Pair(
            convertZonedDateTimeToOffset(period.first, ownerOffset),
            convertZonedDateTimeToOffset(period.second, ownerOffset)
        )

        meeting.schedule.forEach {
            if (isScheduleCrossPeriod(it, fromToPeriod, ownerOffset)) {
                return true
            }
        }
        return false
    }

    private fun isScheduleCrossPeriod(
        schedule: Schedule,
        period: Pair<ZonedDateTime, ZonedDateTime>,
        ownerOffset: String
    ): Boolean {
        val scheduleStart = convertZonedDateTimeToOffset(schedule.startDateTime, ownerOffset)
        val scheduleEnd = scheduleStart.plusSeconds(schedule.duration)
        when (schedule.type) {
            ScheduleType.DAILY -> {
                if (checkPeriod(scheduleStart, schedule.duration, period, 60 * 60 * 24)) {
                    return true
                }
            }
            ScheduleType.DATE -> {
                if (isRangeCrossPeriod(Pair(scheduleStart, scheduleEnd), period)) {
                    return true
                }
            }
            ScheduleType.WORKDAYS -> {
                if (checkPeriod(scheduleStart, schedule.duration, period, 60 * 60 * 24,
                        f = { date -> date.dayOfWeek != DayOfWeek.SUNDAY && date.dayOfWeek != DayOfWeek.SATURDAY }
                    )
                ) {
                    return true
                }
            }
            ScheduleType.WEEKENDS -> {
                if (checkPeriod(scheduleStart, schedule.duration, period, 60 * 60 * 24,
                        f = { date -> date.dayOfWeek == DayOfWeek.SUNDAY || date.dayOfWeek == DayOfWeek.SATURDAY }
                    )
                ) {
                    return true
                }
            }
            ScheduleType.MONDAY -> {
                if (checkPeriod(scheduleStart, schedule.duration, period, 60 * 60 * 24,
                        f = { date -> date.dayOfWeek == DayOfWeek.MONDAY }
                    )
                ) {
                    return true
                }
            }
            ScheduleType.TUESDAY -> {
                if (checkPeriod(scheduleStart, schedule.duration, period, 60 * 60 * 24,
                        f = { date -> date.dayOfWeek == DayOfWeek.TUESDAY }
                    )
                ) {
                    return true
                }
            }
            ScheduleType.WEDNESDAY -> {
                if (checkPeriod(scheduleStart, schedule.duration, period, 60 * 60 * 24,
                        f = { date -> date.dayOfWeek == DayOfWeek.WEDNESDAY }
                    )
                ) {
                    return true
                }
            }
            ScheduleType.THURSDAY -> {
                if (checkPeriod(scheduleStart, schedule.duration, period, 60 * 60 * 24,
                        f = { date -> date.dayOfWeek == DayOfWeek.THURSDAY }
                    )
                ) {
                    return true
                }
            }
            ScheduleType.FRIDAY -> {
                if (checkPeriod(scheduleStart, schedule.duration, period, 60 * 60 * 24,
                        f = { date -> date.dayOfWeek == DayOfWeek.FRIDAY }
                    )
                ) {
                    return true
                }
            }
            ScheduleType.SATURDAY -> {
                if (checkPeriod(scheduleStart, schedule.duration, period, 60 * 60 * 24,
                        f = { date -> date.dayOfWeek == DayOfWeek.SATURDAY }
                    )
                ) {
                    return true
                }
            }
            ScheduleType.SUNDAY -> {
                if (checkPeriod(scheduleStart, schedule.duration, period, 60 * 60 * 24,
                        f = { date -> date.dayOfWeek == DayOfWeek.SUNDAY }
                    )
                ) {
                    return true
                }
            }
            ScheduleType.CUSTOM -> TODO()
        }
        return false
    }

    private fun checkPeriod(
        scheduleStart: ZonedDateTime,
        scheduleDuration: Long,
        period: Pair<ZonedDateTime, ZonedDateTime>,
        steep: Long,
        f: (scheduleStart: ZonedDateTime) -> Boolean = { true }
    ): Boolean {
        var rangeStart = period.first
            .withHour(scheduleStart.hour)
            .withMinute(scheduleStart.minute)
            .withSecond(scheduleStart.second)
            .withNano(0)
        var rangeEnd = rangeStart.plusSeconds(scheduleDuration)

        while (rangeStart.isBefore(period.second)) {
            if (isRangeCrossPeriod(Pair(rangeStart, rangeEnd), period)) {
                if (f(rangeStart)) {
                    return true
                }
            }
            rangeStart = rangeStart.plusSeconds(steep)
            rangeEnd = rangeEnd.plusSeconds(steep)
        }
        return false
    }

    private fun isRangeCrossPeriod(
        range: Pair<ZonedDateTime, ZonedDateTime>,
        period: Pair<ZonedDateTime, ZonedDateTime>
    ): Boolean {
        if (
            (period.first.isBefore(range.first) && period.second.isAfter(range.first)) ||
            (period.first.isBefore(range.second) && period.second.isAfter(range.second)) ||
            (period.first.isAfter(range.first) && period.first.isBefore(range.second)) ||  //for range more than period
            (range.first.isAfter(period.first) && range.second.isBefore(period.second))
        ) {
            return true
        }
        return false
    }
}