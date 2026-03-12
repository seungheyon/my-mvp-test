package com.team1.mvp_test.domain.oauth.client.google

import com.team1.mvp_test.domain.oauth.client.OAuthClient
import com.team1.mvp_test.domain.oauth.client.OAuthLoginUserInfo
import com.team1.mvp_test.domain.oauth.client.google.dto.GoogleOAuthUserInfo
import com.team1.mvp_test.domain.oauth.client.google.dto.GoogleTokenResponse
import com.team1.mvp_test.domain.oauth.exception.InvalidOAuthUserException
import com.team1.mvp_test.domain.oauth.exception.OAuthTokenRetrieveException
import com.team1.mvp_test.domain.oauth.provider.OAuthProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class GoogleOAuthClient(
    @Value("\${oauth2.google.client-id}") private val clientId: String,
    @Value("\${oauth2.google.client-secret}") private val clientSecret: String,
    @Value("\${oauth2.google.redirect-url}") private val redirectUrl: String,
    @Value("\${oauth2.google.auth_server_base_url}") private val authServerBaseUrl: String,
    @Value("\${oauth2.google.resource_server_base_url}") private val resourceServerBaseUrl: String,
    @Value("\${oauth2.google.token_server_base_url}") private val tokenServerBaseUrl: String,
    private val restClient: RestClient
) : OAuthClient {
    override fun generateLoginPageUrl(): String {
        return StringBuilder(authServerBaseUrl)
            .append("?response_type=").append("code")
            .append("&client_id=").append(clientId)
            .append("&redirect_uri=").append(redirectUrl)
            .append("&scope=").append("email")
            .toString()
    }

    override fun getAccessToken(authorizationCode: String): String {
        val requestData = mutableMapOf(
            "grant_type" to "authorization_code",
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "code" to authorizationCode,
            "redirect_uri" to redirectUrl
        )
        return restClient.post()
            .uri("$tokenServerBaseUrl")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(LinkedMultiValueMap<String, String>().apply { this.setAll(requestData) })
            .retrieve()
            .onStatus(HttpStatusCode::isError) { _, _ ->
                throw OAuthTokenRetrieveException(OAuthProvider.GOOGLE)
            }
            .body<GoogleTokenResponse>()
            ?.accessToken
            ?: throw OAuthTokenRetrieveException(OAuthProvider.GOOGLE)
    }

    override fun retrieveUserInfo(accessToken: String): OAuthLoginUserInfo {
        return restClient.get()
            .uri("$resourceServerBaseUrl/v1/userinfo")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .onStatus(HttpStatusCode::isError) { _, _ ->
                throw InvalidOAuthUserException(OAuthProvider.GOOGLE)
            }
            .body<GoogleOAuthUserInfo>()
            ?: throw InvalidOAuthUserException(OAuthProvider.GOOGLE)
    }

    override fun supports(provider: OAuthProvider): Boolean {
        return provider == OAuthProvider.GOOGLE
    }
}