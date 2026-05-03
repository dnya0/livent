package com.livent.user.adapter.outbound.persistence.repository

import com.livent.user.adapter.outbound.persistence.entity.UserEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface UserR2dbcRepository : ReactiveCrudRepository<UserEntity, Long>
