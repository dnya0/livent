package com.livent.message.domain.repository

import com.livent.event.domain.value.ChatRoomId
import com.livent.message.domain.model.Message
import com.livent.message.domain.model.NewMessage
import com.livent.message.domain.value.MessageId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MessageRepository {
    fun findFirstPageByChatRoomId(chatRoomId: ChatRoomId, limit: Int): Flux<Message>

    fun findAfterIdByChatRoomId(chatRoomId: ChatRoomId, cursor: MessageId, limit: Int): Flux<Message>

    fun save(message: NewMessage): Mono<Message>
}
