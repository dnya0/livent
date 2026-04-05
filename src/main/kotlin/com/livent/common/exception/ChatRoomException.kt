package com.livent.common.exception

class ChatRoomNotFoundException : DomainException("채팅방을 찾을 수 없습니다.")
class ChatRoomAccessDeniedException : DomainException("채팅방에 접근할 수 없습니다.")
