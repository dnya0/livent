package com.livent.user.application

import com.livent.common.adapter.inbound.web.exception.InvalidRequestException
import com.livent.user.domain.exception.UserNotFoundException
import com.livent.user.domain.model.User
import com.livent.user.domain.repository.UserRepository
import com.livent.user.domain.value.UserId
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserQueryService(
    private val userRepository: UserRepository,
) {
    fun getUser(userId: Long): Mono<User> = userRepository.findById(resolveUserId(userId))
        .switchIfEmpty(Mono.error(UserNotFoundException()))

    private fun resolveUserId(userId: Long): UserId = try {
        UserId.of(userId)
    } catch (ex: IllegalArgumentException) {
        throw InvalidRequestException("userId must be a positive number.", ex)
    }
}
