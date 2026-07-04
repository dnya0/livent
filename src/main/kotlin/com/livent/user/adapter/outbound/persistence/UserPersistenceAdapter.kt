package com.livent.user.adapter.outbound.persistence

import com.livent.user.adapter.outbound.persistence.entity.UserEntity
import com.livent.user.adapter.outbound.persistence.repository.UserR2dbcRepository
import com.livent.user.domain.model.NewUser
import com.livent.user.domain.model.User
import com.livent.user.domain.repository.UserRepository
import com.livent.user.domain.value.UserId
import com.livent.user.domain.value.UserNickname
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserPersistenceAdapter(
    private val userR2dbcRepository: UserR2dbcRepository,
) : UserRepository {
    override fun findById(id: UserId): Mono<User> = userR2dbcRepository.findById(id.value)
        .map(UserEntity::toDomain)

    override fun existsById(id: UserId): Mono<Boolean> = userR2dbcRepository.existsById(id.value)

    override fun save(user: NewUser): Mono<User> = userR2dbcRepository.save(user.toEntity())
        .map(UserEntity::toDomain)
}

private fun UserEntity.toDomain(): User = User(
    id = UserId.of(id),
    nickname = UserNickname.of(nickname),
)

private fun NewUser.toEntity(id: UserId = UserId.new()): UserEntity = UserEntity(
    id = id.value,
    nickname = nickname.value,
    newEntity = true,
)
