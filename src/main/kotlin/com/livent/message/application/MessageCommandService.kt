package com.livent.message.application

import com.livent.common.adapter.inbound.web.exception.invalidRequestCatch
import com.livent.event.domain.exception.ChatRoomNotFoundException
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.repository.EventRepository
import com.livent.event.domain.value.ChatRoomId
import com.livent.message.domain.model.Message
import com.livent.message.domain.model.NewMessage
import com.livent.message.domain.policy.MessageAccessPolicy
import com.livent.message.domain.repository.MessageRepository
import com.livent.participation.domain.exception.ParticipationNotFoundException
import com.livent.participation.domain.repository.ParticipationRepository
import com.livent.user.domain.value.UserId
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class MessageCommandService(
    private val messageRepository: MessageRepository,
    private val eventRepository: EventRepository,
    private val participationRepository: ParticipationRepository,
) {
    fun sendMessage(chatRoomId: Long, command: SendMessageCommand): Mono<Message> {
        val resolvedChatRoomId = resolveChatRoomId(chatRoomId)
        val resolvedSenderId = resolveUserId(command.senderId, "senderId")
        val newMessage = createTextMessage(
            chatRoomId = resolvedChatRoomId,
            senderId = resolvedSenderId,
            content = command.content,
        )

        return eventRepository.findChatRoomById(resolvedChatRoomId)
            .switchIfEmpty(Mono.error(ChatRoomNotFoundException()))
            .flatMap { chatRoom -> authorizeWrite(chatRoom, resolvedSenderId) }
            .then(messageRepository.save(newMessage))
    }

    private fun authorizeWrite(chatRoom: ChatRoom, senderId: UserId): Mono<ChatRoom> =
        participationRepository.findByEventIdAndUserId(chatRoom.eventId, senderId)
            .switchIfEmpty(Mono.error(ParticipationNotFoundException()))
            .doOnNext { participation -> MessageAccessPolicy.ensureCanWrite(chatRoom, participation) }
            .thenReturn(chatRoom)

    private fun createTextMessage(
        chatRoomId: ChatRoomId,
        senderId: UserId,
        content: String,
    ): NewMessage = invalidRequestCatch(
        message = "content is invalid.",
    ) {
        NewMessage.text(
            chatRoomId = chatRoomId,
            senderId = senderId,
            content = content,
        )
    }
}

internal fun resolveChatRoomId(chatRoomId: Long): ChatRoomId = invalidRequestCatch(
    message = "chatRoomId must be a positive number.",
) {
    ChatRoomId.of(chatRoomId)
}

internal fun resolveUserId(userId: Long, fieldName: String = "userId"): UserId = invalidRequestCatch(
    message = "$fieldName must be a positive number.",
) {
    UserId.of(userId)
}
