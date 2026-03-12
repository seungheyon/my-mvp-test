package com.team1.mvp_test.domain.oauth.client.naver.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.team1.mvp_test.domain.oauth.client.OAuthLoginUserInfo
import com.team1.mvp_test.domain.oauth.provider.OAuthProvider

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class NaverOAuthUserInfo(
    @JsonProperty("resultcode")
    val resultCode: String,
    val message: String,
    val response: NaverUserInfoProperties
) : OAuthLoginUserInfo(
    provider = OAuthProvider.NAVER,
    id = response.id,
    email = response.email
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class NaverUserInfoProperties(
    val id: String,
    val name: String,
    val email: String
)