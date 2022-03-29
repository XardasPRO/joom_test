package com.joom.calendar.calendar.domain.service

import com.joom.calendar.calendar.model.notification.Notification
import org.jboss.logging.Logger
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.PriorityQueue

@Service
@EnableScheduling
class EmailNotificationService : NotificationService {
    val logger = Logger.getLogger(EmailNotificationService::class.java)
    val queue = PriorityQueue { o1: Notification, o2: Notification ->
        o1.deliverAfter.compareTo(o2.deliverAfter)
    }

    override fun notify(notification: Notification) {
        queue.add(notification)
    }

    @Scheduled(fixedRate = 5000)
    fun deliver() {
        var notification = queue.peek()
        while (notification != null && LocalDateTime.now().isAfter(notification.deliverAfter)) {
            logger.info("Notification to email ${notification.user.email}: ${notification.message}")
            queue.remove()
            notification = queue.peek()
        }
    }

}