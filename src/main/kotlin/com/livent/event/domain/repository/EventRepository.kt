package com.livent.event.domain.repository

import com.livent.event.domain.model.Event
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface EventRepository {
    fun findAll(): Flux<Event>

    fun findById(id: Long): Mono<Event>
}
