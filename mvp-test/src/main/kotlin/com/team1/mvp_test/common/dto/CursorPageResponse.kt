package com.team1.mvp_test.common.dto

data class CursorPageResponse<T>(
    val data: List<T>,
    val nextCursor: Long?,
    val hasNext: Boolean
) {
    companion object {
        fun <T> of(items: List<T>, size: Int, idExtractor: (T) -> Long): CursorPageResponse<T> {
            val hasNext = items.size > size
            val data = if (hasNext) items.dropLast(1) else items
            return CursorPageResponse(
                data = data,
                nextCursor = if (hasNext) idExtractor(data.last()) else null,
                hasNext = hasNext
            )
        }
    }
}
