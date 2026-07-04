package com.livent.message.domain.repository

import com.livent.event.domain.value.ChatRoomId
import com.livent.message.domain.model.Message
import com.livent.message.domain.model.NewMessage
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MessageRepository {
    fun findByChatRoomId(chatRoomId: ChatRoomId): Flux<Message>

    fun save(message: NewMessage): Mono<Message>
}
