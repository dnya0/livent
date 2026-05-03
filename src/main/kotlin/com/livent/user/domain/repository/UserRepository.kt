package com.livent.user.domain.repository

import com.livent.user.domain.model.NewUser
import com.livent.user.domain.model.User
import com.livent.user.domain.value.UserId
import reactor.core.publisher.Mono

interface UserRepository {
    fun findById(id: UserId): Mono<User>

    fun save(user: NewUser): Mono<User>
}
