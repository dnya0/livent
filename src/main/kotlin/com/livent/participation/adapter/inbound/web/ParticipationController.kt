package com.livent.participation.adapter.inbound.web

import java.util.UUID
import com.livent.participation.application.ParticipationCommandService
import com.livent.participation.application.ParticipationQueryService
import com.project.common.core.presentation.response.ApiResponse
import com.project.common.core.presentation.response.responseOf
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/events/{eventId}/participations")
class ParticipationController(
    private val participationCommandService: ParticipationCommandService,
    private val participationQueryService: ParticipationQueryService,
) {
    @PostMapping
    fun createParticipation(
        @PathVariable eventId: Long,
        @Valid @RequestBody request: ParticipationCreateRequest,
    ): Mono<ApiResponse<ParticipationResponse>> =
        participationCommandService.createParticipation(eventId = eventId, command = request.toCreateCommand())
            .map { responseOf(it.toResponse()) }

    @GetMapping("/{userId}")
    fun getParticipation(
        @PathVariable eventId: Long,
        @PathVariable userId: UUID,
    ): Mono<ApiResponse<ParticipationResponse>> =
        participationQueryService.getParticipation(eventId = eventId, userId = userId)
            .map { responseOf(it.toResponse()) }
}
