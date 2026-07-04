package com.livent.message.adapter.outbound.persistence

import com.livent.event.domain.value.ChatRoomId
import com.livent.message.adapter.outbound.persistence.entity.MessageEntity
import com.livent.message.adapter.outbound.persistence.repository.MessageR2dbcRepository
import com.livent.message.domain.model.Message
import com.livent.message.domain.model.NewMessage
import com.livent.message.domain.repository.MessageRepository
import com.livent.message.domain.value.MessageContent
import com.livent.message.domain.value.MessageId
import com.livent.user.domain.value.UserId
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class MessagePersistenceAdapter(
    private val messageR2dbcRepository: MessageR2dbcRepository,
) : MessageRepository {
    override fun findByChatRoomId(chatRoomId: ChatRoomId): Flux<Message> =
        messageR2dbcRepository.findByChatRoomIdOrderByIdAsc(chatRoomId.value)
            .map(MessageEntity::toDomain)

    override fun save(message: NewMessage): Mono<Message> =
        messageR2dbcRepository.save(message.toEntity())
            .map(MessageEntity::toDomain)
}

private fun MessageEntity.toDomain(): Message = Message(
    id = MessageId.of(requireNotNull(id) { "Message id must not be null when reading." }),
    chatRoomId = ChatRoomId.of(chatRoomId),
    senderId = UserId.of(senderId),
    content = MessageContent.of(content),
    type = type,
    createdAt = createdAt,
)

private fun NewMessage.toEntity(id: Long? = null): MessageEntity = MessageEntity(
    id = id,
    chatRoomId = chatRoomId.value,
    senderId = senderId.value,
    content = content.value,
    type = type,
    createdAt = createdAt,
)
