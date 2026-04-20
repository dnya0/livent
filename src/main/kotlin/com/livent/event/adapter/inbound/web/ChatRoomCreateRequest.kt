package com.livent.event.adapter.inbound.web

import com.livent.event.application.CreateChatRoomCommand
import com.livent.event.domain.model.type.ChatRoomType
import jakarta.validation.constraints.NotNull

data class ChatRoomCreateRequest(
    @field:NotNull(message = "type must not be null.")
    val type: ChatRoomType,
    val name: String? = null,
)

fun ChatRoomCreateRequest.toCreateCommand(): CreateChatRoomCommand = CreateChatRoomCommand(
    type = type,
    name = name,
)
