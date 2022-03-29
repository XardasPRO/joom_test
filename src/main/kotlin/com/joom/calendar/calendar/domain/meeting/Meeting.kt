package com.joom.calendar.calendar.domain.meeting

import com.joom.calendar.calendar.model.BaseEntity
import com.joom.calendar.calendar.model.schedule.Schedule
import com.joom.calendar.calendar.model.user.User
import java.util.*
import javax.persistence.*

@Entity
class Meeting(
    id: UUID = UUID.randomUUID(),
    val isPrivate: Boolean,
    val name: String,
    val description: String?,
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    val owner: User,
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "meeting_schedule",
        joinColumns = [JoinColumn(name = "meeting_id")],
        inverseJoinColumns = [JoinColumn(name = "schedule_id")]
    )
    val schedule: Set<Schedule>,
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "meeting_id")
    val members: Set<MeetingMember>
) : BaseEntity(id) {
    fun copy(
        id: UUID = this.id,
        isPrivate: Boolean = this.isPrivate,
        name: String = this.name,
        description: String? = this.description,
        owner: User = this.owner,
        schedule: Set<Schedule> = this.schedule,
        members: Set<MeetingMember> = this.members
    ): Meeting {
        return Meeting(id, isPrivate, name, description, owner, schedule, members)
    }
}