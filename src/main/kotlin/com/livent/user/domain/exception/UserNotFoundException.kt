package com.livent.user.domain.exception

import com.project.common.core.domain.exception.DomainException

class UserNotFoundException : DomainException(UserErrorCode.USER_NOT_FOUND)
