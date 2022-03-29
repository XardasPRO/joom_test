package com.joom.calendar.calendar.domain.meeting

import com.joom.calendar.calendar.model.BaseEntity
import com.joom.calendar.calendar.model.user.User
import java.util.*
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.OneToOne

@Entity
class MeetingMember(
    id: UUID = UUID.randomUUID(),
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    val user: User,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    val meeting: Meeting,
    val isConfirmed: Boolean,
    val isCanceled: Boolean
) : BaseEntity(id) {
    fun copy(
        id: UUID = this.id,
        user: User = this.user,
        meeting: Meeting = this.meeting,
        isConfirmed: Boolean = this.isConfirmed,
        isCanceled: Boolean = this.isCanceled
    ): MeetingMember {
        return MeetingMember(id, user, meeting, isConfirmed, isCanceled)
    }
}