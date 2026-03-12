package com.team1.mvp_test.admin.dto.adminauthority

import com.team1.mvp_test.domain.member.model.MemberTest
import java.time.LocalDateTime

data class MvpTestListResponse(
    val testId: Long,
    val mainImageUrl: String,
    val mvpName: String,
    val recruitStartDate: LocalDateTime,
    val recruitEndDate: LocalDateTime,
    val testStartDate: LocalDateTime,
    val testEndDate: LocalDateTime,
) {
    companion object {
        fun from(memberTest: MemberTest): MvpTestListResponse {
            return MvpTestListResponse(
                testId = memberTest.test.id!!,
                mainImageUrl = memberTest.test.mainImageUrl,
                mvpName = memberTest.test.mvpName,
                recruitStartDate = memberTest.test.recruitStartDate,
                recruitEndDate = memberTest.test.recruitEndDate,
                testStartDate = memberTest.test.testStartDate,
                testEndDate = memberTest.test.testEndDate
            )
        }
    }
}