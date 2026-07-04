package com.livent.message.adapter.outbound.persistence.repository

import com.livent.message.adapter.outbound.persistence.entity.MessageEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface MessageR2dbcRepository : ReactiveCrudRepository<MessageEntity, Long> {
    @Query(
        """
        SELECT id, chat_room_id, sender_id, content, type, created_at
        FROM messages
        WHERE chat_room_id = :chatRoomId
        ORDER BY id ASC
        LIMIT :limit
        """,
    )
    fun findFirstPageByChatRoomId(chatRoomId: Long, limit: Int): Flux<MessageEntity>

    @Query(
        """
        SELECT id, chat_room_id, sender_id, content, type, created_at
        FROM messages
        WHERE chat_room_id = :chatRoomId
          AND id > :cursor
        ORDER BY id ASC
        LIMIT :limit
        """,
    )
    fun findAfterIdByChatRoomId(chatRoomId: Long, cursor: Long, limit: Int): Flux<MessageEntity>
}
