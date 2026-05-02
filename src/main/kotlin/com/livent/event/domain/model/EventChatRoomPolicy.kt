package com.livent.event.domain.model

import com.livent.event.domain.type.ChatRoomType
import com.livent.event.domain.type.EventVisibility

object EventChatRoomPolicy {
    fun defaultChatRoomsFor(event: Event): List<NewChatRoom> = defaultTypesFor(event.visibility).map { type ->
        NewChatRoom.create(
            eventId = event.id,
            type = type,
        )
    }

    fun validateCreatable(
        event: Event,
        type: ChatRoomType,
        existingTypes: Set<ChatRoomType>,
    ) {
        require(type in allowedTypes(event.visibility)) {
            "chat room type $type is not allowed for visibility ${event.visibility}."
        }
        require(type !in existingTypes) {
            "chat room type $type already exists for this event."
        }
    }

    private fun allowedTypes(visibility: EventVisibility): Set<ChatRoomType> = when (visibility) {
        EventVisibility.ONSITE,
        EventVisibility.BOTH,
            -> setOf(ChatRoomType.GLOBAL, ChatRoomType.LOCAL, ChatRoomType.SESSION)

        EventVisibility.ONLINE -> setOf(ChatRoomType.GLOBAL, ChatRoomType.SESSION)
    }

    private fun defaultTypesFor(visibility: EventVisibility): List<ChatRoomType> = when (visibility) {
        EventVisibility.ONSITE -> listOf(ChatRoomType.LOCAL)
        EventVisibility.ONLINE -> listOf(ChatRoomType.GLOBAL)
        EventVisibility.BOTH -> listOf(ChatRoomType.GLOBAL, ChatRoomType.LOCAL)
    }
}
