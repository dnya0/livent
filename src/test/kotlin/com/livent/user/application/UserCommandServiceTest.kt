package com.livent.user.application

import kotlin.test.assertEquals
import com.livent.user.domain.model.NewUser
import com.livent.user.domain.model.User
import com.livent.user.domain.repository.UserRepository
import com.livent.user.domain.value.UserId
import com.livent.user.domain.value.UserNickname
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UserCommandServiceTest {
    @Test
    fun `createUser saves user with trimmed nickname`() {
        val repository = FakeUserRepository()
        val service = UserCommandService(repository)

        StepVerifier.create(service.createUser(CreateUserCommand(nickname = "  ahnnayeong  ")))
            .expectNextMatches { user ->
                user.id == UserId.of(1L) &&
                    user.nickname == UserNickname.of("ahnnayeong")
            }
            .verifyComplete()

        assertEquals("ahnnayeong", repository.savedNickname)
    }

    @Test
    fun `createUser rejects blank nickname after normalization`() {
        val service = UserCommandService(FakeUserRepository())

        assertThrows<IllegalArgumentException> {
            service.createUser(CreateUserCommand(nickname = "   "))
        }
    }

    @Test
    fun `createUser rejects too long nickname`() {
        val service = UserCommandService(FakeUserRepository())

        assertThrows<IllegalArgumentException> {
            service.createUser(CreateUserCommand(nickname = "a".repeat(256)))
        }
    }

    private class FakeUserRepository : UserRepository {
        var savedNickname: String? = null

        override fun findById(id: UserId): Mono<User> = Mono.empty()

        override fun save(user: NewUser): Mono<User> {
            savedNickname = user.nickname.value
            return Mono.just(user.persist(UserId.of(1L)))
        }
    }
}
