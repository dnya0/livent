package com.livent.participation.domain.exception

import com.project.common.core.domain.exception.DomainException

class ParticipationAlreadyExistsException : DomainException(ParticipationErrorCode.PARTICIPATION_ALREADY_EXISTS)
