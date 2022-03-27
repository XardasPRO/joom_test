package com.joom.calendar.calendar.domain.service

import com.joom.calendar.calendar.model.security.UniversalAuthenticationToken
import com.joom.calendar.calendar.model.user.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class ContextService {
    fun getAuthorisedUser(): User {
        return (SecurityContextHolder.getContext().authentication as UniversalAuthenticationToken).user
    }
}