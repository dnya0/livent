package com.livent.event.domain.repository

import com.livent.event.domain.model.Event
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface EventRepository {
    fun findFirstPage(limit: Int): Flux<Event>

    fun findAfterId(cursor: Long, limit: Int): Flux<Event>

    fun findById(id: Long): Mono<Event>

    fun existsById(id: Long): Mono<Boolean>
}
