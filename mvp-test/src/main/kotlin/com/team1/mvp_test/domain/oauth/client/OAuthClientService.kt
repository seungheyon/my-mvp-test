package com.team1.mvp_test.domain.oauth.client

import com.team1.mvp_test.domain.oauth.provider.OAuthProvider
import jakarta.transaction.NotSupportedException
import org.springframework.stereotype.Service

@Service
class OAuthClientService(
    private val clients: List<OAuthClient>
) {
    fun generateLoginPageUrl(provider: OAuthProvider): String {
        val client = this.selectClient(provider)
        return client.generateLoginPageUrl()
    }

    fun login(provider: OAuthProvider, authorizationCode: String): OAuthLoginUserInfo {
        val client = this.selectClient(provider)
        return client.getAccessToken(authorizationCode)
            .let { client.retrieveUserInfo(it) }
    }

    private fun selectClient(provider: OAuthProvider): OAuthClient {
        return clients.find { it.supports(provider) } ?: throw NotSupportedException()
    }

}