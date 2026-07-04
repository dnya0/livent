package com.livent.user.adapter.inbound.web

import com.livent.common.adapter.inbound.web.LiventValidationExceptionHandler
import com.livent.user.application.UserCommandService
import com.livent.user.application.UserQueryService
import com.livent.user.domain.model.NewUser
import com.livent.user.domain.model.User
import com.livent.user.domain.repository.UserRepository
import com.livent.user.domain.value.UserId
import com.livent.user.domain.value.UserNickname
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

class UserControllerTest {
    private lateinit var repository: FakeUserRepository
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        repository = FakeUserRepository()
        webTestClient = WebTestClient.bindToController(
            UserController(
                userCommandService = UserCommandService(repository),
                userQueryService = UserQueryService(repository),
            ),
        )
            .controllerAdvice(LiventValidationExceptionHandler())
            .build()
    }

    @Test
    fun `post users returns created user`() {
        webTestClient.post()
            .uri("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "nickname": "ahnnayeong"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.id").isEqualTo("00000000-0000-0000-0000-000000000001")
            .jsonPath("$.data.nickname").isEqualTo("ahnnayeong")
    }

    @Test
    fun `get users returns user`() {
        webTestClient.get()
            .uri("/users/00000000-0000-0000-0000-000000000001")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.id").isEqualTo("00000000-0000-0000-0000-000000000001")
            .jsonPath("$.data.nickname").isEqualTo("ahnnayeong")
    }

    @Test
    fun `post users validates request body`() {
        webTestClient.post()
            .uri("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "nickname": " "
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isBadRequest
    }

    private class FakeUserRepository : UserRepository {
        private var user: User = User(
            id = UserId.of(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")),
            nickname = UserNickname.of("ahnnayeong"),
        )

        override fun findById(id: UserId): Mono<User> =
            user.takeIf { it.id == id }?.let { Mono.just(it) } ?: Mono.empty()

        override fun existsById(id: UserId): Mono<Boolean> = Mono.just(user.id == id)

        override fun save(user: NewUser): Mono<User> {
            this.user = user.persist(UserId.of(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")))
            return Mono.just(this.user)
        }
    }
}
