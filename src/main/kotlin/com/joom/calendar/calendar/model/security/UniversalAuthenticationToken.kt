package com.joom.calendar.calendar.model.security

import com.joom.calendar.calendar.model.user.User
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class UniversalAuthenticationToken(
    authorities: MutableCollection<out GrantedAuthority>?,
    val user: User
) : AbstractAuthenticationToken(
    authorities
) {
    override fun getCredentials(): Any {
        return user.password
    }

    override fun getPrincipal(): Any {
        return user
    }
}