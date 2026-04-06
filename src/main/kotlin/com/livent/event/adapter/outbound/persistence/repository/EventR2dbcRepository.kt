package com.livent.event.adapter.outbound.persistence.repository

import com.livent.event.adapter.outbound.persistence.entity.EventEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface EventR2dbcRepository : ReactiveCrudRepository<EventEntity, Long>
