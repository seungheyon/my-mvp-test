package com.team1.mvp_test.domain.mvptest.service

import com.team1.mvp_test.domain.category.repository.CategoryRepository
import com.team1.mvp_test.domain.enterprise.model.Enterprise
import com.team1.mvp_test.domain.enterprise.model.EnterpriseState
import com.team1.mvp_test.domain.enterprise.repository.EnterpriseRepository
import com.team1.mvp_test.domain.member.model.Member
import com.team1.mvp_test.domain.member.model.MemberState
import com.team1.mvp_test.domain.member.model.Sex
import com.team1.mvp_test.domain.member.repository.MemberRepository
import com.team1.mvp_test.domain.member.repository.MemberTestRepository
import com.team1.mvp_test.domain.member.service.MemberService
import com.team1.mvp_test.domain.mvptest.model.MvpTest
import com.team1.mvp_test.domain.mvptest.model.MvpTestState
import com.team1.mvp_test.domain.mvptest.model.RecruitType
import com.team1.mvp_test.domain.mvptest.repository.MvpTestCategoryMapRepository
import com.team1.mvp_test.domain.mvptest.repository.MvpTestRepository
import com.team1.mvp_test.infra.querydsl.QueryDslConfig
import com.team1.mvp_test.infra.redisson.RedissonService
import com.team1.mvp_test.infra.s3.s3service.S3ServiceImpl
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDateTime

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(value = [QueryDslConfig::class])
@ContextConfiguration(classes = [MvpTestTestConfig::class])
class MvpTestIntegrationTest @Autowired constructor(
    private val mvpTestRepository: MvpTestRepository,
    private val categoryRepository: CategoryRepository,
    private val mvpTestCategoryMapRepository: MvpTestCategoryMapRepository,
    private val memberRepository: MemberRepository,
    private val memberTestRepository: MemberTestRepository,
    private val redissonService: RedissonService,
    private val s3Service: S3ServiceImpl,
    private val memberService: MemberService,
    private val enterpriseRepository: EnterpriseRepository,
) {
    private val mvpTestService = MvpTestService(
        mvpTestRepository,
        categoryRepository,
        mvpTestCategoryMapRepository,
        memberRepository,
        memberTestRepository,
        redissonService,
        s3Service,
        memberService,
        enterpriseRepository
    )

    @Test
    fun getAvailableTestsTest() {
        //given
        val pageable = PageRequest.of(0, 5)
        val member = Member(
            id = 1L,
            name = "test",
            email = "test@test.com",
            age = 20,
            sex = Sex.MALE,
            state = MemberState.ACTIVE,
        )
        val enterprise = Enterprise(
            id = 1L,
            email = "test@test.com",
            name = "testName",
            ceoName = "testCeoName",
            password = "pWdjH0wrRuybq9ccRSDug2Z",
            phoneNumber = "01012345678",
            state = EnterpriseState.APPROVED,
            reason = null
        )
        val test1 = MvpTest(
            id = 1L,
            enterpriseId = 1L,
            mvpName = "string",
            recruitStartDate = LocalDateTime.of(2025, 5, 1, 12, 0),
            recruitEndDate = LocalDateTime.of(2025, 5, 5, 12, 0),
            testStartDate = LocalDateTime.of(2025, 5, 10, 12, 0),
            testEndDate = LocalDateTime.of(2025, 5, 15, 12, 0),
            mainImageUrl = "string.jpg",
            mvpInfo = "string",
            mvpUrl = "string",
            rewardBudget = 100000,
            requirementMinAge = 25,
            requirementMaxAge = 60,
            requirementSex = Sex.MALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 50,
            state = MvpTestState.APPROVED
        )
        val test2 = MvpTest(
            id = 2L,
            enterpriseId = 1L,
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
        val test3 = MvpTest(
            id = 3L,
            enterpriseId = 1L,
            mvpName = "string",
            recruitStartDate = LocalDateTime.of(2025, 5, 1, 12, 0),
            recruitEndDate = LocalDateTime.of(2025, 5, 5, 12, 0),
            testStartDate = LocalDateTime.of(2025, 5, 10, 12, 0),
            testEndDate = LocalDateTime.of(2025, 5, 15, 12, 0),
            mainImageUrl = "string.jpg",
            mvpInfo = "string",
            mvpUrl = "string",
            rewardBudget = 100000,
            requirementMinAge = 0,
            requirementMaxAge = 25,
            requirementSex = null,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 50,
            state = MvpTestState.APPROVED
        )
        val test4 = MvpTest(
            id = 4L,
            enterpriseId = 1L,
            mvpName = "string",
            recruitStartDate = LocalDateTime.of(2025, 5, 1, 12, 0),
            recruitEndDate = LocalDateTime.of(2025, 5, 5, 12, 0),
            testStartDate = LocalDateTime.of(2025, 5, 10, 12, 0),
            testEndDate = LocalDateTime.of(2025, 5, 15, 12, 0),
            mainImageUrl = "string.jpg",
            mvpInfo = "string",
            mvpUrl = "string",
            rewardBudget = 100000,
            requirementMinAge = 10,
            requirementMaxAge = 16,
            requirementSex = Sex.MALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 50,
            state = MvpTestState.APPROVED
        )
        val test5 = MvpTest(
            id = 5L,
            enterpriseId = 1L,
            mvpName = "string",
            recruitStartDate = LocalDateTime.of(2025, 5, 1, 12, 0),
            recruitEndDate = LocalDateTime.of(2025, 5, 5, 12, 0),
            testStartDate = LocalDateTime.of(2025, 5, 10, 12, 0),
            testEndDate = LocalDateTime.of(2025, 5, 15, 12, 0),
            mainImageUrl = "string.jpg",
            mvpInfo = "string",
            mvpUrl = "string",
            rewardBudget = 100000,
            requirementMinAge = null,
            requirementMaxAge = null,
            requirementSex = Sex.MALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 50,
            state = MvpTestState.APPROVED
        )
        val test6 = MvpTest(
            id = 1L,
            enterpriseId = 1L,
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
            requirementSex = Sex.FEMALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 50,
            state = MvpTestState.APPROVED
        )
        memberRepository.save(member)
        enterpriseRepository.save(enterprise)
        mvpTestRepository.save(test1)
        mvpTestRepository.save(test2)
        mvpTestRepository.save(test3)
        mvpTestRepository.save(test4)
        mvpTestRepository.save(test5)
        mvpTestRepository.save(test6)

        //when
        val availableTests = mvpTestService.getAvailableTests(member.id!!, pageable)
        //then
        availableTests.size shouldBe 3
        availableTests[0].id shouldBe 5
        availableTests[1].id shouldBe 3
        availableTests[2].id shouldBe 2
    }
}