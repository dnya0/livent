package com.livent.event.application

import java.time.Instant
import com.livent.event.domain.type.ChatRoomType
import com.livent.event.domain.type.EventVisibility

data class CreateEventCommand(
    val title: String,
    val location: String,
    val startTime: Instant,
    val endTime: Instant,
    val timezone: String,
    val visibility: EventVisibility,
)

data class UpdateEventCommand(
    val title: String,
    val location: String,
    val startTime: Instant,
    val endTime: Instant,
    val timezone: String,
    val visibility: EventVisibility,
)

data class CreateChatRoomCommand(
    val type: ChatRoomType,
    val name: String? = null,
)
