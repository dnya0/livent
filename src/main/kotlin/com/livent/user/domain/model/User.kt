package com.livent.user.domain.model

import com.livent.user.domain.value.UserId
import com.livent.user.domain.value.UserNickname

data class User(
    val id: UserId,
    val nickname: UserNickname,
)

data class NewUser(
    val nickname: UserNickname,
) {
    fun persist(id: UserId): User = User(
        id = id,
        nickname = nickname,
    )

    companion object {
        fun create(nickname: String): NewUser = NewUser(
            nickname = UserNickname.of(nickname),
        )
    }
}
