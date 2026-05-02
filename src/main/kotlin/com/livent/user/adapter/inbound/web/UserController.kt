package com.livent.user.adapter.inbound.web

import com.livent.user.application.UserCommandService
import com.livent.user.application.UserQueryService
import com.project.common.core.presentation.response.ApiResponse
import com.project.common.core.presentation.response.responseOf
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/users")
class UserController(
    private val userCommandService: UserCommandService,
    private val userQueryService: UserQueryService,
) {
    @PostMapping
    fun createUser(@Valid @RequestBody request: UserCreateRequest): Mono<ApiResponse<UserResponse>> =
        userCommandService.createUser(request.toCreateCommand())
            .map { responseOf(it.toResponse()) }

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: Long): Mono<ApiResponse<UserResponse>> =
        userQueryService.getUser(userId)
            .map { responseOf(it.toResponse()) }
}
