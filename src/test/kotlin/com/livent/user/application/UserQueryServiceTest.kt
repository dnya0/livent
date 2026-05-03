package com.livent.user.application

import com.livent.common.adapter.inbound.web.exception.InvalidRequestException
import com.livent.user.domain.exception.UserNotFoundException
import com.livent.user.domain.model.NewUser
import com.livent.user.domain.model.User
import com.livent.user.domain.repository.UserRepository
import com.livent.user.domain.value.UserId
import com.livent.user.domain.value.UserNickname
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UserQueryServiceTest {
    @Test
    fun `getUser returns user when found`() {
        val service = UserQueryService(
            FakeUserRepository(
                user = User(
                    id = UserId.of(1L),
                    nickname = UserNickname.of("ahnnayeong"),
                ),
            ),
        )

        StepVerifier.create(service.getUser(1L))
            .expectNextMatches { it.id == UserId.of(1L) && it.nickname == UserNickname.of("ahnnayeong") }
            .verifyComplete()
    }

    @Test
    fun `getUser throws UserNotFoundException when missing`() {
        val service = UserQueryService(FakeUserRepository(user = null))

        StepVerifier.create(service.getUser(999L))
            .expectError(UserNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `getUser rejects non positive userId`() {
        val service = UserQueryService(FakeUserRepository(user = null))

        assertThrows<InvalidRequestException> {
            service.getUser(0L)
        }
    }

    private class FakeUserRepository(
        private val user: User?,
    ) : UserRepository {
        override fun findById(id: UserId): Mono<User> =
            user?.takeIf { it.id == id }?.let { Mono.just(it) } ?: Mono.empty()

        override fun save(user: NewUser): Mono<User> = Mono.just(user.persist(UserId.of(1L)))
    }
}
