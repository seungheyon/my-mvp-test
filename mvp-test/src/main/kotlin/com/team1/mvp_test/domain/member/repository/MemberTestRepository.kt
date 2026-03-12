package com.team1.mvp_test.domain.member.repository

import com.team1.mvp_test.domain.member.model.MemberTest
import com.team1.mvp_test.domain.member.model.MemberTestState
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MemberTestRepository : JpaRepository<MemberTest, Long> {
    fun findByMemberIdAndTestId(memberId: Long, testId: Long): MemberTest?
    fun countByTestIdAndState(testId: Long, state: MemberTestState): Long
    fun findAllByTestId(testId: Long): List<MemberTest>
    fun findAllByMemberId(memberId: Long): List<MemberTest>
    fun existsByMemberIdAndTestId(memberId: Long, testId: Long): Boolean

    @Query("SELECT mt FROM MemberTest mt JOIN FETCH mt.member WHERE mt.test.id = :testId")
    fun findAllAndMemberByTestId(testId: Long): List<MemberTest>
}