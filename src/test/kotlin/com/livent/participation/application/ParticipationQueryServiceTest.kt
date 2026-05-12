package com.livent.participation.application

import java.time.Instant
import com.livent.common.adapter.inbound.web.exception.InvalidRequestException
import com.livent.event.domain.value.EventId
import com.livent.participation.domain.exception.ParticipationNotFoundException
import com.livent.participation.domain.model.NewParticipation
import com.livent.participation.domain.model.Participation
import com.livent.participation.domain.repository.ParticipationRepository
import com.livent.participation.domain.type.ParticipationStatus
import com.livent.participation.domain.value.ParticipationId
import com.livent.user.domain.value.UserId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ParticipationQueryServiceTest {
    @Test
    fun `getParticipation returns participation when found`() {
        val service = ParticipationQueryService(
            FakeParticipationRepository(
                participation = Participation(
                    id = ParticipationId.of(1L),
                    userId = UserId.of(1L),
                    eventId = EventId.of(1L),
                    status = ParticipationStatus.ONLINE,
                    joinedAt = Instant.parse("2026-05-12T00:00:00Z"),
                ),
            ),
        )

        StepVerifier.create(service.getParticipation(1L, 1L))
            .expectNextMatches {
                it.id == ParticipationId.of(1L) &&
                    it.userId == UserId.of(1L) &&
                    it.eventId == EventId.of(1L)
            }
            .verifyComplete()
    }

    @Test
    fun `getParticipation throws ParticipationNotFoundException when missing`() {
        val service = ParticipationQueryService(FakeParticipationRepository(participation = null))

        StepVerifier.create(service.getParticipation(1L, 1L))
            .expectError(ParticipationNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `getParticipation rejects non positive eventId`() {
        val service = ParticipationQueryService(FakeParticipationRepository(participation = null))

        assertThrows<InvalidRequestException> {
            service.getParticipation(0L, 1L)
        }
    }

    @Test
    fun `getParticipation rejects non positive userId`() {
        val service = ParticipationQueryService(FakeParticipationRepository(participation = null))

        assertThrows<InvalidRequestException> {
            service.getParticipation(1L, 0L)
        }
    }

    private class FakeParticipationRepository(
        private val participation: Participation?,
    ) : ParticipationRepository {
        override fun findByEventIdAndUserId(eventId: EventId, userId: UserId): Mono<Participation> =
            participation
                ?.takeIf { it.eventId == eventId && it.userId == userId }
                ?.let { Mono.just(it) }
                ?: Mono.empty()

        override fun save(participation: NewParticipation): Mono<Participation> =
            Mono.just(participation.persist(ParticipationId.of(1L)))
    }
}
