package com.livent.event.adapter.inbound.web

import java.time.Instant
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.model.Event
import com.livent.event.domain.model.type.ChatRoomType
import com.livent.event.domain.model.type.EventVisibility

data class EventResponse(
    val id: Long,
    val title: String,
    val location: String,
    val startTime: Instant,
    val endTime: Instant,
    val timezone: String,
    val visibility: EventVisibility,
)

data class ChatRoomResponse(
    val id: Long,
    val eventId: Long,
    val type: ChatRoomType,
    val name: String,
)

fun Event.toResponse(): EventResponse = EventResponse(
    id = id,
    title = title,
    location = location,
    startTime = schedule.startTime,
    endTime = schedule.endTime,
    timezone = schedule.timezone.id,
    visibility = visibility,
)

fun ChatRoom.toResponse(): ChatRoomResponse = ChatRoomResponse(
    id = id,
    eventId = eventId,
    type = type,
    name = name,
)
