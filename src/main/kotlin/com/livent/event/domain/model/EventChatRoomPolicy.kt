package com.livent.event.domain.model

import com.livent.event.domain.model.type.ChatRoomType
import com.livent.event.domain.model.type.EventVisibility

object EventChatRoomPolicy {
    fun defaultChatRoomsFor(event: Event): List<NewChatRoom> = listOf(
        NewChatRoom.create(
            eventId = event.id,
            type = ChatRoomType.GLOBAL,
        ),
    )

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
}
