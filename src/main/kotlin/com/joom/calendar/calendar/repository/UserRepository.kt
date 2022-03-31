package com.joom.calendar.calendar.repository

import com.joom.calendar.calendar.model.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findUserByLogin(login: String): Optional<User>
    fun findUserByEmail(email: String): Optional<User>
}