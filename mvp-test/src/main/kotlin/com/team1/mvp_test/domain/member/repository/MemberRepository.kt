package com.team1.mvp_test.domain.member.repository

import com.team1.mvp_test.domain.member.model.Member
import com.team1.mvp_test.domain.oauth.provider.OAuthProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    fun existsByEmail(email: String): Boolean
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    fun findByProviderIdAndProviderName(providerId: String, providerName: OAuthProvider): Member?
}