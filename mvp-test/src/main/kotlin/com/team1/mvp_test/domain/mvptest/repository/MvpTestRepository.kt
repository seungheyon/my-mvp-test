package com.team1.mvp_test.domain.mvptest.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.team1.mvp_test.domain.member.model.Member
import com.team1.mvp_test.domain.member.model.MemberTestState
import com.team1.mvp_test.domain.member.model.QMember
import com.team1.mvp_test.domain.member.model.QMemberTest
import com.team1.mvp_test.domain.mvptest.dto.MemberInfoResponse
import com.team1.mvp_test.domain.mvptest.model.MvpTest
import com.team1.mvp_test.domain.mvptest.model.MvpTestState
import com.team1.mvp_test.domain.mvptest.model.QMvpTest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Repository
interface MvpTestRepository : JpaRepository<MvpTest, Long>, MvpTestQueryDslRepository {
    fun findAllByEnterpriseId(enterpriseId: Long): List<MvpTest>
}

interface MvpTestQueryDslRepository {
    fun findAllUnsettledMvpTests(date: LocalDate): List<MvpTest>
    fun findMemberList(testId: Long, enterpriseId: Long): List<MemberInfoResponse>
    fun findAvailableTests(member: Member, pageable: Pageable): List<MvpTest>
    fun findMvpTestListByCursor(cursor: Long?, size: Int): List<MvpTest>
}

class MvpTestQueryDslRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : MvpTestQueryDslRepository {

    private val mvpTest: QMvpTest = QMvpTest.mvpTest
    private val memberTest: QMemberTest = QMemberTest.memberTest
    private val member: QMember = QMember.member

    override fun findAllUnsettledMvpTests(date: LocalDate): List<MvpTest> {
        val builder = BooleanBuilder()
            .and(mvpTest.testEndDate.before(date.atTime(LocalTime.MAX)))
            .and(mvpTest.settlementDate.isNull)
        return queryFactory.selectFrom(mvpTest)
            .where(builder)
            .fetch()
    }

    override fun findMemberList(testId: Long, enterpriseId: Long): List<MemberInfoResponse> {
        return queryFactory
            .select(
                Projections.constructor(
                    MemberInfoResponse::class.java,
                    member.id,
                    member.email,
                    member.phoneNumber,
                    member.name,
                )
            )
            .from(memberTest)
            .join(memberTest.member, member)
            .where(
                memberTest.test.id.eq(testId)
                    .and(memberTest.test.enterpriseId.eq(enterpriseId))
                    .and(memberTest.state.eq(MemberTestState.APPROVED))
            )
            .fetch()
    }

    override fun findAvailableTests(member: Member, pageable: Pageable): List<MvpTest> {
        val builder = BooleanBuilder()
            .and(mvpTest.state.eq(MvpTestState.APPROVED))
            .and(mvpTest.recruitEndDate.after(LocalDateTime.now()))
            .and(mvpTest.requirementMinAge.isNull.or(mvpTest.requirementMinAge.loe(member.age)))
            .and(mvpTest.requirementMaxAge.isNull.or(mvpTest.requirementMaxAge.goe(member.age)))
            .and(mvpTest.requirementSex.isNull.or(mvpTest.requirementSex.eq(member.sex)))

        return queryFactory.selectFrom(mvpTest)
            .where(builder)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(mvpTest.id.desc())
            .fetch()
    }

    override fun findMvpTestListByCursor(cursor: Long?, size: Int): List<MvpTest> {
        val builder = BooleanBuilder()
        cursor?.let { builder.and(mvpTest.id.lt(it)) }

        return queryFactory.selectFrom(mvpTest)
            .where(builder)
            .orderBy(mvpTest.id.desc())
            .limit((size + 1).toLong())
            .fetch()
    }
}
