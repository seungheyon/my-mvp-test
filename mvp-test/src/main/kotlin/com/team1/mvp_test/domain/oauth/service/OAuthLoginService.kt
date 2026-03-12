package com.team1.mvp_test.domain.oauth.service

import com.team1.mvp_test.common.Role
import com.team1.mvp_test.domain.member.dto.LoginResponse
import com.team1.mvp_test.domain.member.model.MemberState
import com.team1.mvp_test.domain.member.service.MemberService
import com.team1.mvp_test.domain.oauth.client.OAuthClientService
import com.team1.mvp_test.domain.oauth.provider.OAuthProvider
import com.team1.mvp_test.infra.security.jwt.JwtHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OAuthLoginService(
    private val oAuthClientService: OAuthClientService,
    private val memberService: MemberService,
    private val jwtHelper: JwtHelper
) {
    @Transactional
    fun login(provider: OAuthProvider, code: String): LoginResponse {
        return oAuthClientService.login(provider, code)
            .let { memberService.registerIfAbsent(it) }
            .let {
                val accessToken = jwtHelper.generateAccessToken(
                    subject = it.id!!.toString(),
                    email = it.email,
                    role = Role.MEMBER.name
                )
                val isActiveMember = it.state == MemberState.ACTIVE
                LoginResponse(accessToken, isActiveMember)
            }
    }
}