package com.team1.mvp_test.domain.member.repository

import com.team1.mvp_test.domain.member.model.MemberReward
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRewardRepository : JpaRepository<MemberReward, Long> {
    fun findAllByMemberId(memberId: Long): List<MemberReward>
}