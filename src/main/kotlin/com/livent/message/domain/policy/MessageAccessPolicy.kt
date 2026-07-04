package com.livent.message.domain.policy

import com.livent.event.domain.exception.ChatRoomAccessDeniedException
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.type.ChatRoomType
import com.livent.participation.domain.model.Participation
import com.livent.participation.domain.type.ParticipationStatus

object MessageAccessPolicy {
    fun ensureCanRead(chatRoom: ChatRoom, participation: Participation) {
        ensureSameEvent(chatRoom, participation)
        ensureLocalParticipant(chatRoom, participation)
    }

    fun ensureCanWrite(chatRoom: ChatRoom, participation: Participation) {
        ensureSameEvent(chatRoom, participation)
        ensureLocalParticipant(chatRoom, participation)
    }

    private fun ensureSameEvent(chatRoom: ChatRoom, participation: Participation) {
        if (participation.eventId != chatRoom.eventId) {
            throw ChatRoomAccessDeniedException()
        }
    }

    private fun ensureLocalParticipant(chatRoom: ChatRoom, participation: Participation) {
        if (chatRoom.type == ChatRoomType.LOCAL && participation.status != ParticipationStatus.ONSITE) {
            throw ChatRoomAccessDeniedException()
        }
    }
}
