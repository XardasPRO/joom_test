package com.joom.calendar.calendar.model.user

import com.joom.calendar.calendar.model.BaseEntity
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "user_authorities", schema = "public")
class UserAuthority(
    id: UUID = UUID.randomUUID(),
    @OneToOne
    val user: User,
    val authority: String
) : BaseEntity() {
}