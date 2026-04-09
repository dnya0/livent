package com.livent.event.domain.exception

import com.project.common.core.domain.exception.DomainException

class ChatRoomNotFoundException : DomainException(EventErrorCode.CHAT_ROOM_NOT_FOUND)
