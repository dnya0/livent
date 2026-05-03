package com.livent.user.adapter.inbound.web

import com.livent.user.application.CreateUserCommand

typealias UserCreateRequest = UserPayload

fun UserCreateRequest.toCreateCommand(): CreateUserCommand = CreateUserCommand(
    nickname = nickname,
)
