package com.livent.common.exception

class AlreadyParticipatingException : DomainException("이미 참여 중인 이벤트입니다.")
class ParticipationNotFoundException : DomainException("참여 정보를 찾을 수 없습니다.")
