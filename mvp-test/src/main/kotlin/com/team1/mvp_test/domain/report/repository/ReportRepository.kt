package com.team1.mvp_test.domain.report.repository

import com.team1.mvp_test.domain.member.model.MemberTest
import com.team1.mvp_test.domain.report.model.Report
import com.team1.mvp_test.domain.report.model.ReportState
import com.team1.mvp_test.domain.step.model.Step
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ReportRepository : JpaRepository<Report, Long> {
    fun existsByStepAndMemberTest(step: Step, memberTest: MemberTest): Boolean
    fun findAllByMemberTestAndState(memberTest: MemberTest, state: ReportState): List<Report>
    @Query("SELECT r FROM Report r JOIN FETCH r.memberTest mt JOIN FETCH mt.member WHERE r.step.id = :stepId")
    fun findAllByStepId(@Param("stepId") stepId: Long): List<Report>
    fun findByStepIdAndMemberTestId(stepId: Long, memberTestId: Long): Report?
}