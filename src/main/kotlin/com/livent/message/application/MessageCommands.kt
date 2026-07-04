package com.livent.message.application

data class SendMessageCommand(
    val senderId: Long,
    val content: String,
)
