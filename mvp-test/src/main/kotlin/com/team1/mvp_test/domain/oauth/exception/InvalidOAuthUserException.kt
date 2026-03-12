package com.team1.mvp_test.domain.oauth.exception

import com.team1.mvp_test.domain.oauth.provider.OAuthProvider

data class InvalidOAuthUserException(val provider: OAuthProvider) : RuntimeException(
    "Failed to retrieve userinfo from ${provider.name}"
)