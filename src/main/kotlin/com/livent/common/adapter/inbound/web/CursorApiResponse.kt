package com.livent.common.adapter.inbound.web

data class CursorApiResponse<T>(
    val code: String = "SUCCESS",
    val message: String = "요청이 성공했습니다.",
    val data: List<T>,
    val pageInfo: CursorPageInfo,
)

data class CursorPageInfo(
    val size: Int,
    val hasNext: Boolean,
    val nextCursor: String?,
)
