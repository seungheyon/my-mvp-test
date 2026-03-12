package com.team1.mvp_test.domain.oauth.exception

import com.team1.mvp_test.domain.oauth.provider.OAuthProvider

data class OAuthTokenRetrieveException(val provider: OAuthProvider) : RuntimeException(
    "Failed to retrieve access token from ${provider.name}"
)