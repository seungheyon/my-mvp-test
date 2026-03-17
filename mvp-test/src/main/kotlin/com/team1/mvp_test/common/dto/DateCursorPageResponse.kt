package com.team1.mvp_test.common.dto

import java.time.LocalDateTime

data class DateCursorPageResponse<T>(
    val data: List<T>,
    val nextCursorDate: LocalDateTime?,
    val nextCursorId: Long?,
    val hasNext: Boolean
) {
    companion object {
        fun <T> of(
            items: List<T>,
            size: Int,
            dateExtractor: (T) -> LocalDateTime,
            idExtractor: (T) -> Long
        ): DateCursorPageResponse<T> {
            val hasNext = items.size > size
            val data = if (hasNext) items.dropLast(1) else items
            return DateCursorPageResponse(
                data = data,
                nextCursorDate = if (hasNext) dateExtractor(data.last()) else null,
                nextCursorId = if (hasNext) idExtractor(data.last()) else null,
                hasNext = hasNext
            )
        }
    }
}
