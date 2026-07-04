package com.livent.message.adapter.inbound.web

import java.util.UUID
import com.livent.message.application.SendMessageCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class MessageCreateRequest(
    @field:NotNull(message = "senderId must not be null.")
    val senderId: UUID,
    @field:NotBlank(message = "content must not be blank.")
    @field:Size(max = 1000, message = "content must be 1000 characters or less.")
    val content: String,
) {
    fun toCommand(): SendMessageCommand = SendMessageCommand(
        senderId = senderId,
        content = content,
    )
}
