package com.team1.mvp_test.domain.oauth.controller

import com.team1.mvp_test.domain.member.dto.LoginResponse
import com.team1.mvp_test.domain.oauth.client.OAuthClientService
import com.team1.mvp_test.domain.oauth.provider.OAuthProvider
import com.team1.mvp_test.domain.oauth.service.OAuthLoginService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/member/oauth")
class MemberOAuthController(
    private val oAuthLoginService: OAuthLoginService,
    private val oAuthClientService: OAuthClientService,
) {
    @GetMapping("/{provider}/login")
    fun redirectLoginPage(
        @PathVariable provider: OAuthProvider,
    ): String {
        return oAuthClientService.generateLoginPageUrl(provider)
    }

    @GetMapping("/{provider}/callback")
    fun callback(
        @PathVariable provider: OAuthProvider,
        @RequestParam code: String,
    ): ResponseEntity<LoginResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(oAuthLoginService.login(provider, code))
    }
}