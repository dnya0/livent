package com.livent.user.application

import java.util.UUID
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
    fun getUser(userId: UUID): Mono<User> = userRepository.findById(UserId.of(userId))
        .switchIfEmpty(Mono.error(UserNotFoundException()))
}
