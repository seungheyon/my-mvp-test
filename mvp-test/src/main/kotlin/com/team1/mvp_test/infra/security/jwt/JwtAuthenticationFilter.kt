package com.team1.mvp_test.infra.security.jwt

import com.team1.mvp_test.infra.security.UserPrincipal
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtHelper: JwtHelper
) : OncePerRequestFilter() {

    companion object {
        private val BEARER_PATTERN = Regex("^Bearer (.+?)$")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val jwt = request.getBearer()
        if (jwt != null) {
            jwtHelper.validateToken(jwt)
                .onSuccess {
                    UserPrincipal(
                        id = it.payload.subject.toLong(),
                        email = it.payload.get("email", String::class.java),
                        roles = setOf(it.payload.get("role", String::class.java))
                    ).let { principal ->
                        JwtAuthenticationToken(
                            principal = principal,
                            details = WebAuthenticationDetailsSource().buildDetails(request)
                        )
                    }.let { authentication ->
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }

        }

        filterChain.doFilter(request, response)
    }

    private fun HttpServletRequest.getBearer(): String? {
        return this.getHeader(HttpHeaders.AUTHORIZATION)?.let {
            BEARER_PATTERN.find(it)?.groupValues?.get(1)
        }
    }
}