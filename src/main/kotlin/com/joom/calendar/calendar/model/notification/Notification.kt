package com.joom.calendar.calendar.model.notification

import com.joom.calendar.calendar.model.user.User
import java.time.LocalDateTime

class Notification(
    val user: User,
    val message: String,
    val deliverAfter: LocalDateTime
)