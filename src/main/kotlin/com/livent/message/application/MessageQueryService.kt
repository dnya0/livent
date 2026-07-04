package com.livent.message.application

import java.util.UUID
import com.livent.common.adapter.inbound.web.exception.InvalidRequestException
import com.livent.event.domain.exception.ChatRoomNotFoundException
import com.livent.event.domain.repository.EventRepository
import com.livent.event.domain.value.ChatRoomId
import com.livent.message.domain.model.Message
import com.livent.message.domain.policy.MessageAccessPolicy
import com.livent.message.domain.repository.MessageRepository
import com.livent.message.domain.value.MessageId
import com.livent.participation.domain.exception.ParticipationNotFoundException
import com.livent.participation.domain.repository.ParticipationRepository
import com.livent.user.domain.value.UserId
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class MessageQueryService(
    private val messageRepository: MessageRepository,
    private val eventRepository: EventRepository,
    private val participationRepository: ParticipationRepository,
) {
    fun getMessageSlice(chatRoomId: Long, userId: UUID, cursor: String?, size: Int): Mono<MessageSlice<Message>> {
        val resolvedChatRoomId = resolveChatRoomId(chatRoomId)
        val resolvedUserId = UserId.of(userId)
        val request = resolveSliceRequest(cursor = cursor, size = size)

        return eventRepository.findChatRoomById(resolvedChatRoomId)
            .switchIfEmpty(Mono.error(ChatRoomNotFoundException()))
            .flatMap { chatRoom ->
                participationRepository.findByEventIdAndUserId(chatRoom.eventId, resolvedUserId)
                    .switchIfEmpty(Mono.error(ParticipationNotFoundException()))
                    .flatMap { participation ->
                        MessageAccessPolicy.ensureCanRead(chatRoom, participation)
                        requestMessages(chatRoomId = resolvedChatRoomId, request = request)
                            .collectList()
                            .map { toMessageSlice(results = it, pageSize = request.pageSize) }
                    }
            }
    }

    companion object {
        const val DEFAULT_PAGE_SIZE: Int = 20
        const val MAX_PAGE_SIZE: Int = 100
    }

    private fun requestMessages(chatRoomId: ChatRoomId, request: MessageSliceRequest): Flux<Message> =
        if (request.cursorId == null) {
            messageRepository.findFirstPageByChatRoomId(chatRoomId = chatRoomId, limit = request.fetchLimit)
        } else {
            messageRepository.findAfterIdByChatRoomId(
                chatRoomId = chatRoomId,
                cursor = request.cursorId,
                limit = request.fetchLimit,
            )
        }

    private fun resolveSliceRequest(cursor: String?, size: Int): MessageSliceRequest {
        if (size <= 0) {
            throw InvalidRequestException("size must be greater than 0.")
        }

        val pageSize = size.coerceAtMost(MAX_PAGE_SIZE)
        return MessageSliceRequest(
            cursorId = decodeCursor(cursor),
            pageSize = pageSize,
            fetchLimit = pageSize + 1,
        )
    }

    private fun toMessageSlice(results: List<Message>, pageSize: Int): MessageSlice<Message> {
        val hasNext = results.size > pageSize
        val items = if (hasNext) results.take(pageSize) else results
        val nextCursor = if (hasNext) items.lastOrNull()?.id?.value?.toString() else null

        return MessageSlice(
            items = items,
            size = items.size,
            hasNext = hasNext,
            nextCursor = nextCursor,
        )
    }

    private fun decodeCursor(cursor: String?): MessageId? {
        if (cursor == null) return null

        val raw = cursor.toLongOrNull()
            ?.takeIf { it > 0 }
            ?: throw InvalidRequestException("cursor must be a positive number.")

        return MessageId.of(raw)
    }
}

private data class MessageSliceRequest(
    val cursorId: MessageId?,
    val pageSize: Int,
    val fetchLimit: Int,
)

data class MessageSlice<T>(
    val items: List<T>,
    val size: Int,
    val hasNext: Boolean,
    val nextCursor: String?,
)
