package com.joom.calendar.calendar.model.user

import com.joom.calendar.calendar.model.BaseEntity
import com.joom.calendar.calendar.model.schedule.Schedule
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "users")
class User(
    id: UUID = UUID.randomUUID(),
    val name: String,
    val surname: String,
    val login: String,
    val password: String,
    val email: String?,
    val zoneOffset: String,
    val isEnabled: Boolean,
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    val authorities: Set<UserAuthority> = emptySet(),
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_working_schedule",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "schedule_id")]
    )
    val schedule: Set<Schedule> = emptySet()
) : BaseEntity(id) {

    fun copy(
        id: UUID = this.id,
        name: String = this.name,
        surname: String = this.surname,
        login: String = this.login,
        password: String = this.password,
        email: String? = this.email,
        zoneOffset: String = this.zoneOffset,
        isEnabled: Boolean = this.isEnabled,
        authorities: Set<UserAuthority> = this.authorities,
        schedule: Set<Schedule> = this.schedule
    ): User {
        return User(
            id, name, surname, login, password, email, zoneOffset, isEnabled, authorities, schedule
        )
    }
}