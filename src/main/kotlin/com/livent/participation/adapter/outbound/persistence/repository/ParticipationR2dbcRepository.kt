package com.livent.participation.adapter.outbound.persistence.repository

import java.util.UUID
import com.livent.participation.adapter.outbound.persistence.entity.ParticipationEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface ParticipationR2dbcRepository : ReactiveCrudRepository<ParticipationEntity, Long> {
    fun findByEventIdAndUserId(eventId: Long, userId: UUID): Mono<ParticipationEntity>
}
