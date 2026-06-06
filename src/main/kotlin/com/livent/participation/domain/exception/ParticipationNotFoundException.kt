package com.livent.participation.domain.exception

import com.project.common.core.domain.exception.DomainException

class ParticipationNotFoundException : DomainException(ParticipationErrorCode.PARTICIPATION_NOT_FOUND)
