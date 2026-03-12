package com.team1.mvp_test.domain.step.service

import com.team1.mvp_test.domain.member.model.Member
import com.team1.mvp_test.domain.member.model.MemberTest
import com.team1.mvp_test.domain.member.model.Sex
import com.team1.mvp_test.domain.member.repository.MemberRepository
import com.team1.mvp_test.domain.member.repository.MemberTestRepository
import com.team1.mvp_test.domain.mvptest.model.MvpTest
import com.team1.mvp_test.domain.mvptest.model.MvpTestState
import com.team1.mvp_test.domain.mvptest.model.RecruitType
import com.team1.mvp_test.domain.mvptest.repository.MvpTestRepository
import com.team1.mvp_test.domain.report.model.Report
import com.team1.mvp_test.domain.report.model.ReportState
import com.team1.mvp_test.domain.report.repository.ReportRepository
import com.team1.mvp_test.domain.step.model.Step
import com.team1.mvp_test.domain.step.repository.StepRepository
import com.team1.mvp_test.infra.querydsl.QueryDslConfig
import com.team1.mvp_test.infra.s3.s3service.S3ServiceImpl
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDateTime


@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(value = [QueryDslConfig::class])
@ContextConfiguration(classes = [StepTestConfig::class])
class StepIntegrationTest @Autowired constructor(
    private val stepRepository: StepRepository,
    private val mvpTestRepository: MvpTestRepository,
    private val s3Service: S3ServiceImpl,
    private val memberTestRepository: MemberTestRepository,
    private val reportRepository: ReportRepository,
    private val memberRepository: MemberRepository,
) {
    private val memberReportService = MemberReportService(memberTestRepository, reportRepository)
    private val stepService =
        StepService(
            stepRepository,
            mvpTestRepository,
            s3Service,
            memberReportService,
            memberRepository,
            reportRepository,
            memberTestRepository
        )

    private val stepId = 1L
    private val testId = 1L
    private val enterpriseId = 1L
    private val mvpTest = MvpTest(
        id = testId,
        enterpriseId = enterpriseId,
        mvpName = "string",
        recruitStartDate = LocalDateTime.of(2025, 5, 1, 12, 0),
        recruitEndDate = LocalDateTime.of(2025, 5, 5, 12, 0),
        testStartDate = LocalDateTime.of(2025, 5, 10, 12, 0),
        testEndDate = LocalDateTime.of(2025, 5, 15, 12, 0),
        mainImageUrl = "string.jpg",
        mvpInfo = "string",
        mvpUrl = "string",
        rewardBudget = 100000,
        requirementMinAge = 15,
        requirementMaxAge = 60,
        requirementSex = Sex.MALE,
        recruitType = RecruitType.FIRST_COME,
        recruitNum = 50,
        state = MvpTestState.APPROVED
    )
    private val step = Step(
        id = stepId,
        mvpTest = mvpTest,
        requirement = "test",
        reward = 100,
        stepOrder = 1,
        title = "test",
    )
    private val member1 = Member(
        id = 1L,
        name = "test",
        email = "test@test.com",
        age = 20,
        sex = Sex.MALE,
    )
    private val member2 = Member(
        id = 2L,
        name = "test",
        email = "test@test.com",
        age = 20,
        sex = Sex.MALE,
    )
    private val member3 = Member(
        id = 3L,
        name = "test",
        email = "test@test.com",
        age = 20,
        sex = Sex.MALE,
    )
    private val member4 = Member(
        id = 4L,
        name = "test",
        email = "test@test.com",
        age = 20,
        sex = Sex.MALE,
    )

    @BeforeEach
    fun setup() {

        val savedMember1 = memberRepository.save(member1)
        val savedMember2 = memberRepository.save(member2)
        val savedMember3 = memberRepository.save(member3)
        val savedMember4 = memberRepository.save(member4)

        val savedMvpTest = mvpTestRepository.save(mvpTest)

        val savedMemberTest1 = memberTestRepository.save(MemberTest(1L, savedMember1, savedMvpTest))
        val savedMemberTest2 = memberTestRepository.save(MemberTest(2L, savedMember2, savedMvpTest))
        val savedMemberTest3 = memberTestRepository.save(MemberTest(3L, savedMember3, savedMvpTest))
        val savedMemberTest4 = memberTestRepository.save(MemberTest(4L, savedMember4, savedMvpTest))

        val savedStep = stepRepository.save(step)

        val report1 = Report(
            id = 1L,
            title = "test",
            body = "test",
            feedback = "test",
            memberTest = savedMemberTest1,
            step = savedStep,
            state = ReportState.APPROVED
        )
        val report2 = Report(
            id = 2L,
            title = "test",
            body = "test",
            feedback = "test",
            memberTest = savedMemberTest2,
            step = savedStep,
            state = ReportState.PENDING
        )
        val report3 = Report(
            id = 3L,
            title = "test",
            body = "test",
            feedback = "test",
            memberTest = savedMemberTest3,
            step = savedStep,
            state = ReportState.REJECTED
        )

        reportRepository.save(report1)
        reportRepository.save(report2)
        reportRepository.save(report3)

    }

    @AfterEach
    fun tearDown() {
        reportRepository.deleteAll()
        memberTestRepository.deleteAll()
        stepRepository.deleteAll()
        mvpTestRepository.deleteAll()
        memberTestRepository.deleteAll()
    }

    @Test
    fun getStepOverviewTest() {

        //given&when
        val overview = stepService.getStepOverview(enterpriseId, stepId)

        //then
        overview.reportStatusList.size shouldBe 4
        overview.completionRate shouldBe 25
        overview.reportStatusList.first().completionState shouldBe ReportState.APPROVED
        overview.reportStatusList.last().completionState shouldBe ReportState.MISSING
    }
}
