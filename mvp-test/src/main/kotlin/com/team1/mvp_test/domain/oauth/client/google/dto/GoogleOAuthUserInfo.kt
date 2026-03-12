package com.team1.mvp_test.domain.oauth.client.google.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.team1.mvp_test.domain.oauth.client.OAuthLoginUserInfo
import com.team1.mvp_test.domain.oauth.provider.OAuthProvider

data class GoogleOAuthUserInfo(
    @JsonProperty("id") val providerId: String,
    @JsonProperty("email") val googleEmail: String,
) : OAuthLoginUserInfo(
    provider = OAuthProvider.GOOGLE,
    id = providerId,
    email = googleEmail,
)