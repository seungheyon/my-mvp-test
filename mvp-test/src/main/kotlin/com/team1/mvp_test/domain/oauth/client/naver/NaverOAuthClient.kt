package com.team1.mvp_test.domain.oauth.client.naver

import com.team1.mvp_test.domain.oauth.client.OAuthClient
import com.team1.mvp_test.domain.oauth.client.OAuthLoginUserInfo
import com.team1.mvp_test.domain.oauth.client.naver.dto.NaverOAuthUserInfo
import com.team1.mvp_test.domain.oauth.client.naver.dto.NaverTokenResponse
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
class NaverOAuthClient(
    @Value("\${oauth2.naver.client_id}") val clientId: String,
    @Value("\${oauth2.naver.client_secret}") val clientSecret: String,
    @Value("\${oauth2.naver.redirect_url}") val redirectUrl: String,
    @Value("\${oauth2.naver.auth_server_base_url}") val authServerBaseUrl: String,
    @Value("\${oauth2.naver.resource_server_base_url}") val resourceServerBaseUrl: String,
    private val restClient: RestClient
) : OAuthClient {
    override fun generateLoginPageUrl(): String {
        return StringBuilder(authServerBaseUrl)
            .append("/oauth2.0/authorize")
            .append("?response_type=").append("code")
            .append("&client_id=").append(clientId)
            .append("&redirect_uri=").append(redirectUrl)
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
            .uri("$authServerBaseUrl/oauth2.0/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(LinkedMultiValueMap<String, String>().apply { this.setAll(requestData) })
            .retrieve()
            .onStatus(HttpStatusCode::isError) { _, _ ->
                throw OAuthTokenRetrieveException(OAuthProvider.NAVER)
            }
            .body<NaverTokenResponse>()
            ?.accessToken
            ?: throw OAuthTokenRetrieveException(OAuthProvider.NAVER)
    }

    override fun retrieveUserInfo(accessToken: String): OAuthLoginUserInfo {
        return restClient.get()
            .uri("$resourceServerBaseUrl/v1/nid/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .onStatus(HttpStatusCode::isError) { _, _ ->
                throw InvalidOAuthUserException(OAuthProvider.NAVER)
            }
            .body<NaverOAuthUserInfo>()
            ?: throw InvalidOAuthUserException(OAuthProvider.NAVER)
    }

    override fun supports(provider: OAuthProvider): Boolean {
        return provider == OAuthProvider.NAVER
    }
}