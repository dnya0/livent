package com.livent.message.application

import com.livent.event.domain.exception.ChatRoomNotFoundException
import com.livent.event.domain.repository.EventRepository
import com.livent.message.domain.model.Message
import com.livent.message.domain.policy.MessageAccessPolicy
import com.livent.message.domain.repository.MessageRepository
import com.livent.participation.domain.exception.ParticipationNotFoundException
import com.livent.participation.domain.repository.ParticipationRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class MessageQueryService(
    private val messageRepository: MessageRepository,
    private val eventRepository: EventRepository,
    private val participationRepository: ParticipationRepository,
) {
    fun getMessages(chatRoomId: Long, userId: Long): Flux<Message> {
        val resolvedChatRoomId = resolveChatRoomId(chatRoomId)
        val resolvedUserId = resolveUserId(userId)

        return eventRepository.findChatRoomById(resolvedChatRoomId)
            .switchIfEmpty(Mono.error(ChatRoomNotFoundException()))
            .flatMapMany { chatRoom ->
                participationRepository.findByEventIdAndUserId(chatRoom.eventId, resolvedUserId)
                    .switchIfEmpty(Mono.error(ParticipationNotFoundException()))
                    .flatMapMany { participation ->
                        MessageAccessPolicy.ensureCanRead(chatRoom, participation)
                        messageRepository.findByChatRoomId(resolvedChatRoomId)
                    }
            }
    }
}
