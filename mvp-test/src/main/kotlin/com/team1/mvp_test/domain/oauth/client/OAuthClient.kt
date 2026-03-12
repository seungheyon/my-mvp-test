package com.team1.mvp_test.domain.oauth.client

import com.team1.mvp_test.domain.oauth.provider.OAuthProvider

interface OAuthClient {
    fun generateLoginPageUrl(): String
    fun getAccessToken(authorizationCode: String): String
    fun retrieveUserInfo(accessToken: String): OAuthLoginUserInfo
    fun supports(provider: OAuthProvider): Boolean
}