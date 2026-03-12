package com.team1.mvp_test.domain.oauth.client

import com.team1.mvp_test.domain.oauth.provider.OAuthProvider

open class OAuthLoginUserInfo(
    val provider: OAuthProvider,
    val id: String,
    val email: String,
)