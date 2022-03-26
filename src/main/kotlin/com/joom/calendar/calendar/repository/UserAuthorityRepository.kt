package com.joom.calendar.calendar.repository

import com.joom.calendar.calendar.model.user.UserAuthority
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserAuthorityRepository : JpaRepository<UserAuthority, UUID> {
}