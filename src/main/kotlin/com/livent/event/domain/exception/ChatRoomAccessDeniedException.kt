package com.livent.event.domain.exception

import com.project.common.core.domain.exception.DomainException

class ChatRoomAccessDeniedException : DomainException(EventErrorCode.CHAT_ROOM_ACCESS_DENIED)
