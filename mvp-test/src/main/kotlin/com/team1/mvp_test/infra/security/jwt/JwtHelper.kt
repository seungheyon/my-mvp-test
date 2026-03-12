package com.team1.mvp_test.infra.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.*


@Component
class JwtHelper(
    @Value("\${auth.jwt.issuer}") private val issuer: String,
    @Value("\${auth.jwt.accessTokenSecret}") private val accessTokenSecret: String,
    @Value("\${auth.jwt.accessTokenExpirationHour}") private val accessTokenExpirationHour: Int,
) {

    fun validateToken(jwt: String): Result<Jws<Claims>> {
        return kotlin.runCatching {
            Keys.hmacShaKeyFor(accessTokenSecret.toByteArray(StandardCharsets.UTF_8))
                .let { Jwts.parser().verifyWith(it).build().parseSignedClaims(jwt) }
        }
    }

    fun generateAccessToken(subject: String, email: String, role: String): String {
        return generateToken(subject, email, role, Duration.ofHours(accessTokenExpirationHour.toLong()))
    }

    private fun generateToken(subject: String, email: String, role: String, expirationPeriod: Duration): String {
        val claims = Jwts.claims().add(mapOf("email" to email, "role" to role)).build()
        val now = Instant.now()
        val key = Keys.hmacShaKeyFor(accessTokenSecret.toByteArray(StandardCharsets.UTF_8))

        return Jwts.builder()
            .subject(subject)
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expirationPeriod)))
            .claims(claims)
            .signWith(key)
            .compact()
    }
}