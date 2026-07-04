package com.livent.message.adapter.inbound.web

import com.livent.message.application.MessageCommandService
import com.livent.message.application.MessageQueryService
import com.project.common.core.presentation.response.ApiResponse
import com.project.common.core.presentation.response.responseOf
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@Validated
@RestController
@RequestMapping("/chat-rooms/{chatRoomId}/messages")
class MessageController(
    private val messageCommandService: MessageCommandService,
    private val messageQueryService: MessageQueryService,
) {
    @PostMapping
    fun sendMessage(
        @PathVariable chatRoomId: Long,
        @Valid @RequestBody request: MessageCreateRequest,
    ): Mono<ApiResponse<MessageResponse>> =
        messageCommandService.sendMessage(chatRoomId = chatRoomId, command = request.toCommand())
            .map { responseOf(it.toResponse()) }

    @GetMapping
    fun getMessages(
        @PathVariable chatRoomId: Long,
        @RequestParam @Positive(message = "userId must be a positive number.") userId: Long,
    ): Mono<ApiResponse<List<MessageResponse>>> =
        messageQueryService.getMessages(chatRoomId = chatRoomId, userId = userId)
            .map { it.toResponse() }
            .collectList()
            .map { responseOf(it) }
}
