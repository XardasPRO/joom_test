package com.joom.calendar.calendar.rest.utils

import com.joom.calendar.calendar.model.schedule.Schedule
import com.joom.calendar.calendar.model.schedule.ScheduleType
import com.joom.calendar.calendar.model.user.User
import com.joom.calendar.calendar.rest.dto.schedule.ScheduleDto
import com.joom.calendar.calendar.rest.dto.user.UserDto
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

class EntityMapper {
    companion object {
        fun userToDto(user: User): UserDto {
            return UserDto(
                id = user.id,
                login = user.login,
                name = user.name,
                zoneOffset = user.zoneOffset
            )
        }

        fun scheduleDtoToEntity(dto: ScheduleDto, zoneOffset: String): Schedule {
            return Schedule(
                id = UUID.randomUUID(),
                type = ScheduleType.valueOf(dto.type),
                startDateTime = dto.startDateTime.atZone(ZoneId.ofOffset("UTC", ZoneOffset.of(zoneOffset)))
                    .withZoneSameInstant(ZoneId.of("UTC")),
                duration = dto.duration,
                isRepeatable = dto.isRepeatable
            )
        }
    }
}