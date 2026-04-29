package com.livent.event.domain.exception

import com.project.common.core.domain.exception.DomainException

class ChatRoomAlreadyExistsException : DomainException(EventErrorCode.CHAT_ROOM_ALREADY_EXISTS)
