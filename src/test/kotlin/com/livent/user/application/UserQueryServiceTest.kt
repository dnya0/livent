package com.livent.user.application

import java.util.UUID
import com.livent.user.domain.exception.UserNotFoundException
import com.livent.user.domain.model.NewUser
import com.livent.user.domain.model.User
import com.livent.user.domain.repository.UserRepository
import com.livent.user.domain.value.UserId
import com.livent.user.domain.value.UserNickname
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UserQueryServiceTest {
    private val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val missingUserId = UUID.fromString("00000000-0000-0000-0000-000000000999")

    @Test
    fun `getUser returns user when found`() {
        val service = UserQueryService(
            FakeUserRepository(
                user = User(
                    id = UserId.of(userId),
                    nickname = UserNickname.of("ahnnayeong"),
                ),
            ),
        )

        StepVerifier.create(service.getUser(userId))
            .expectNextMatches { it.id == UserId.of(userId) && it.nickname == UserNickname.of("ahnnayeong") }
            .verifyComplete()
    }

    @Test
    fun `getUser throws UserNotFoundException when missing`() {
        val service = UserQueryService(FakeUserRepository(user = null))

        StepVerifier.create(service.getUser(missingUserId))
            .expectError(UserNotFoundException::class.java)
            .verify()
    }

    private class FakeUserRepository(
        private val user: User?,
    ) : UserRepository {
        override fun findById(id: UserId): Mono<User> =
            user?.takeIf { it.id == id }?.let { Mono.just(it) } ?: Mono.empty()

        override fun existsById(id: UserId): Mono<Boolean> =
            Mono.just(user?.id == id)

        override fun save(user: NewUser): Mono<User> =
            Mono.just(user.persist(UserId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"))))
    }
}
