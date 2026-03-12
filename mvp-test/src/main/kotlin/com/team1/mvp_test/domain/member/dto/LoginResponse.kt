package com.team1.mvp_test.domain.member.dto

data class LoginResponse(
    val accessToken: String,
    val isActiveMember: Boolean
)
