package com.livent.participation.adapter.outbound.persistence

import java.lang.reflect.Proxy
import com.livent.event.domain.value.EventId
import com.livent.participation.adapter.outbound.persistence.entity.ParticipationEntity
import com.livent.participation.adapter.outbound.persistence.repository.ParticipationR2dbcRepository
import com.livent.participation.domain.exception.ParticipationAlreadyExistsException
import com.livent.participation.domain.model.NewParticipation
import com.livent.participation.domain.type.ParticipationStatus
import com.livent.user.domain.value.UserId
import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ParticipationPersistenceAdapterTest {
    @Test
    fun `save maps unique constraint violation to conflict domain error`() {
        val adapter = ParticipationPersistenceAdapter(
            participationR2dbcRepository = stubRepository(
                saveResult = Mono.error(
                    R2dbcDataIntegrityViolationException(
                        "duplicate key value violates unique constraint uk_participations_event_user",
                        "23505",
                        0,
                    ),
                ),
            ),
        )

        StepVerifier.create(
            adapter.save(
                NewParticipation.create(
                    userId = UserId.of(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")),
                    eventId = EventId.of(1L),
                    status = ParticipationStatus.ONLINE,
                ),
            ),
        )
            .expectError(ParticipationAlreadyExistsException::class.java)
            .verify()
    }

    @Test
    fun `save preserves non constraint failures`() {
        val failure = IllegalStateException("boom")
        val adapter = ParticipationPersistenceAdapter(
            participationR2dbcRepository = stubRepository(saveResult = Mono.error(failure)),
        )

        StepVerifier.create(
            adapter.save(
                NewParticipation.create(
                    userId = UserId.of(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")),
                    eventId = EventId.of(1L),
                ),
            ),
        )
            .expectErrorMatches { it === failure }
            .verify()
    }

    @Suppress("UNCHECKED_CAST")
    private fun stubRepository(saveResult: Mono<ParticipationEntity>): ParticipationR2dbcRepository =
        Proxy.newProxyInstance(
            ParticipationR2dbcRepository::class.java.classLoader,
            arrayOf(ParticipationR2dbcRepository::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "save" -> saveResult
                "findByEventIdAndUserId" -> Mono.empty<ParticipationEntity>()
                "findById" -> Mono.empty<ParticipationEntity>()
                "existsById" -> Mono.just(false)
                "deleteById" -> Mono.empty<Void>()
                else -> throw UnsupportedOperationException("Unexpected call: ${method.name}")
            }
        } as ParticipationR2dbcRepository
}
