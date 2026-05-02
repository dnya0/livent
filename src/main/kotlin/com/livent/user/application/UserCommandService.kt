package com.livent.user.application

import com.livent.user.domain.model.NewUser
import com.livent.user.domain.model.User
import com.livent.user.domain.repository.UserRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserCommandService(
    private val userRepository: UserRepository,
) {
    fun createUser(command: CreateUserCommand): Mono<User> = userRepository.save(
        NewUser.create(nickname = command.nickname),
    )
}
