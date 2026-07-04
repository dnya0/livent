package com.livent.user.adapter.outbound.persistence.entity

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class UserEntity(
    @Id
    private val id: UUID,
    val nickname: String,
    @Transient
    private val newEntity: Boolean = false,
) : Persistable<UUID> {
    override fun getId(): UUID = id

    override fun isNew(): Boolean = newEntity
}
