package com.joom.calendar.calendar.model

import java.util.UUID
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class BaseEntity(
    @Id
    val id: UUID = UUID.randomUUID()
){
}