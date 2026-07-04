package com.livent.message.adapter.inbound.web

import java.util.UUID
import com.livent.message.application.MessageCommandService
import com.livent.message.application.MessageQueryService
import com.livent.message.application.MessageSlice
import com.livent.message.domain.model.Message
import com.project.common.core.presentation.response.ApiResponse
import com.project.common.core.presentation.response.CursorApiResponse
import com.project.common.core.presentation.response.CursorPageInfo
import com.project.common.core.presentation.response.responseOf
import com.project.webflux.presentation.request.CursorRequest
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
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
    ): Mono<ApiResponse<MessageResponse>> = messageCommandService.sendMessage(
        chatRoomId = chatRoomId,
        command = request.toCommand()
    ).map { responseOf(it.toResponse()) }

    @GetMapping
    fun getMessages(
        @PathVariable chatRoomId: Long,
        @RequestParam userId: UUID,
        @Valid @ModelAttribute cursorRequest: CursorRequest,
    ): Mono<CursorApiResponse<MessageResponse>> = messageQueryService.getMessageSlice(
        chatRoomId = chatRoomId,
        userId = userId,
        cursor = cursorRequest.cursor,
        size = cursorRequest.size,
    ).map(::toCursorResponse)

    private fun toCursorResponse(slice: MessageSlice<Message>): CursorApiResponse<MessageResponse> = CursorApiResponse(
        data = slice.items.map { it.toResponse() },
        pageInfo = CursorPageInfo(
            size = slice.size,
            hasNext = slice.hasNext,
            nextCursor = slice.nextCursor,
        ),
    )
}
