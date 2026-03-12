package com.team1.mvp_test.domain.mvptest.dto

data class MemberInfoResponse(
    val memberId: Long,
    val email: String,
    val phoneNumber: String,
    val name: String,
)