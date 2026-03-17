package com.team1.mvp_test.domain.report.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.team1.mvp_test.domain.member.model.MemberTest
import com.team1.mvp_test.domain.member.model.QMember
import com.team1.mvp_test.domain.member.model.QMemberTest
import com.team1.mvp_test.domain.report.model.QReport
import com.team1.mvp_test.domain.report.model.Report
import com.team1.mvp_test.domain.report.model.ReportState
import com.team1.mvp_test.domain.step.model.Step
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ReportRepository : JpaRepository<Report, Long>, ReportQueryDslRepository {
    fun existsByStepAndMemberTest(step: Step, memberTest: MemberTest): Boolean
    fun findAllByMemberTestAndState(memberTest: MemberTest, state: ReportState): List<Report>
    @Query("SELECT r FROM Report r JOIN FETCH r.memberTest mt JOIN FETCH mt.member WHERE r.step.id = :stepId")
    fun findAllByStepId(@Param("stepId") stepId: Long): List<Report>
    fun findByStepIdAndMemberTestId(stepId: Long, memberTestId: Long): Report?
}

interface ReportQueryDslRepository {
    fun findReportListByStepCursor(stepId: Long, cursor: Long?, size: Int): List<Report>
}

class ReportQueryDslRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : ReportQueryDslRepository {

    private val report: QReport = QReport.report
    private val memberTest: QMemberTest = QMemberTest.memberTest
    private val member: QMember = QMember.member

    override fun findReportListByStepCursor(stepId: Long, cursor: Long?, size: Int): List<Report> {
        val builder = BooleanBuilder()
            .and(report.step.id.eq(stepId))
        cursor?.let { builder.and(report.id.lt(it)) }

        return queryFactory.selectFrom(report)
            .join(report.memberTest, memberTest).fetchJoin()
            .join(memberTest.member, member).fetchJoin()
            .where(builder)
            .orderBy(report.id.desc())
            .limit((size + 1).toLong())
            .fetch()
    }
}