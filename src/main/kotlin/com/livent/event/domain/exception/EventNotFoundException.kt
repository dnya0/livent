package com.livent.event.domain.exception

import com.project.common.core.domain.exception.DomainException

class EventNotFoundException : DomainException(EventErrorCode.EVENT_NOT_FOUND)
