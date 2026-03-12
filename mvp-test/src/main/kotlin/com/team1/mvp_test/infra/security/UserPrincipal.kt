package com.team1.mvp_test.infra.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class UserPrincipal(
    val id: Long,
    val email: String,
    val authorities: Collection<GrantedAuthority>
) {
    constructor(id: Long, email: String, roles: Set<String>) : this(
        id,
        email,
        roles.map { SimpleGrantedAuthority("ROLE_$it") })
}