package com.joom.calendar.calendar.model.user

import com.joom.calendar.calendar.model.BaseEntity
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "users", schema = "public")
class User(
    id: UUID = UUID.randomUUID(),
    val name: String,
    val surname: String,
    val login: String,
    val password: String,
    val email: String?,
    val timezone: Short,
    val isEnabled: Boolean,
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    val authorities: Set<UserAuthority>
) : BaseEntity(id) {

    fun copy(
        id: UUID = this.id,
        name: String = this.name,
        surname: String = this.surname,
        login: String = this.login,
        password: String = this.password,
        email: String? = this.email,
        timezone: Short = this.timezone,
        isEnabled: Boolean = this.isEnabled,
        authorities: Set<UserAuthority> = this.authorities
    ): User {
        return User(
            id, name, surname, login, password, email, timezone, isEnabled, authorities
        )
    }
}