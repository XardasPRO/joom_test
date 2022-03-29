package com.joom.calendar.calendar.domain.service

import com.joom.calendar.calendar.model.notification.Notification

interface NotificationService {
    fun notify(notification: Notification)
}