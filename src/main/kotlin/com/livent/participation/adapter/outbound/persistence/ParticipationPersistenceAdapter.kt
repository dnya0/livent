package com.livent.participation.adapter.outbound.persistence

import com.livent.event.domain.value.EventId
import com.livent.participation.adapter.outbound.persistence.entity.ParticipationEntity
import com.livent.participation.adapter.outbound.persistence.repository.ParticipationR2dbcRepository
import com.livent.participation.domain.exception.ParticipationAlreadyExistsException
import com.livent.participation.domain.model.NewParticipation
import com.livent.participation.domain.model.Participation
import com.livent.participation.domain.repository.ParticipationRepository
import com.livent.participation.domain.value.ParticipationId
import com.livent.user.domain.value.UserId
import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ParticipationPersistenceAdapter(
    private val participationR2dbcRepository: ParticipationR2dbcRepository,
) : ParticipationRepository {
    override fun findByEventIdAndUserId(eventId: EventId, userId: UserId): Mono<Participation> =
        participationR2dbcRepository.findByEventIdAndUserId(eventId.value, userId.value)
            .map(ParticipationEntity::toDomain)

    override fun save(participation: NewParticipation): Mono<Participation> =
        participationR2dbcRepository.save(participation.toEntity())
            .onErrorMap(::isParticipationUniqueViolation) { ParticipationAlreadyExistsException() }
            .map(ParticipationEntity::toDomain)
}

private fun isParticipationUniqueViolation(ex: Throwable): Boolean {
    val errors = generateSequence(ex) { it.cause }.toList()
    val isIntegrityViolation = errors.any {
        it is DuplicateKeyException ||
            it is DataIntegrityViolationException ||
            it is R2dbcDataIntegrityViolationException
    }
    val hasParticipationUniqueConstraint = errors
        .mapNotNull(Throwable::message)
        .any { message ->
            message.contains("uk_participations_event_user", ignoreCase = true) ||
                message.contains("duplicate key", ignoreCase = true) ||
                message.contains("23505")
        }

    return isIntegrityViolation && hasParticipationUniqueConstraint
}

private fun ParticipationEntity.toDomain(): Participation = Participation(
    id = ParticipationId.of(requireNotNull(id) { "Participation id must not be null when reading." }),
    userId = UserId.of(userId),
    eventId = EventId.of(eventId),
    status = status,
    joinedAt = joinedAt,
)

private fun NewParticipation.toEntity(id: Long? = null): ParticipationEntity = ParticipationEntity(
    id = id,
    userId = userId.value,
    eventId = eventId.value,
    status = status,
    joinedAt = joinedAt,
)
