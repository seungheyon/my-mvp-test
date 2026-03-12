package com.team1.mvp_test.domain.oauth.client.google.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String
)
