package com.livent.message.domain.exception

import com.project.common.core.domain.exception.DomainException

class MessageNotFoundException : DomainException(MessageErrorCode.MESSAGE_NOT_FOUND)
