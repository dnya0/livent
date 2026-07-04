package com.livent.message.application

import java.util.UUID

data class SendMessageCommand(
    val senderId: UUID,
    val content: String,
)
