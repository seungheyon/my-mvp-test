package com.team1.mvp_test.domain.oauth.client.naver.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NaverTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String
)