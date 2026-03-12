package com.team1.mvp_test.domain.mvptest.service.apply

import com.team1.mvp_test.domain.category.repository.CategoryRepository
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
import com.team1.mvp_test.domain.mvptest.service.MvpTestService
import com.team1.mvp_test.infra.querydsl.QueryDslConfig
import com.team1.mvp_test.infra.redisson.RedissonService
import com.team1.mvp_test.infra.s3.s3service.S3ServiceImpl
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(value = [QueryDslConfig::class])
@ActiveProfiles("test")
@ContextConfiguration(classes = [RedissonTestConfig::class])
class ConcurrencyControl @Autowired constructor(
    private val mvpTestRepository: MvpTestRepository,
    private val categoryRepository: CategoryRepository,
    private val mvpTestCategoryMapRepository: MvpTestCategoryMapRepository,
    private val memberRepository: MemberRepository,
    private val memberTestRepository: MemberTestRepository,
    @Autowired
    private val redissonService: RedissonService,
    private val s3Service: S3ServiceImpl,
    private val memberService: MemberService,
    private val enterpriseRepository: EnterpriseRepository,
) {
    private var mvpTestService = MvpTestService(
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

    @BeforeEach
    fun setup() {
        memberRepository.deleteAll()
        mvpTestRepository.deleteAll()
        memberTestRepository.deleteAll()
    }


    // 테스트 실행 전에 로컬에서 6380 포트가 비어있는지 확인해주세요
    @Test
    fun testConcurrencyControl() {
        val threadCount = 100
        val recruitNum = 50

        val executor = Executors.newFixedThreadPool(threadCount)
        val barrier = CyclicBarrier(threadCount)

        repeat(threadCount) {
            memberRepository.save(
                Member(
                    id = it.toLong() + 1L,
                    email = "test@test.test",
                    sex = Sex.MALE,
                    state = MemberState.ACTIVE
                )
            )
        }

        val test = MvpTest(
            id = 1,
            enterpriseId = 1,
            mvpName = "test mvp name",
            recruitStartDate = LocalDateTime.of(2024, 5, 1, 12, 0),
            recruitEndDate = LocalDateTime.of(2025, 5, 5, 12, 0),
            testStartDate = LocalDateTime.of(2025, 5, 10, 12, 0),
            testEndDate = LocalDateTime.of(2025, 5, 15, 12, 0),
            mainImageUrl = "test mvp main url",
            mvpInfo = "test mvp info",
            mvpUrl = "test mvp url",
            rewardBudget = 10000,
            requirementMinAge = null,
            requirementMaxAge = null,
            requirementSex = Sex.MALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = recruitNum,
            state = MvpTestState.APPROVED
        ).let { mvpTestRepository.save(it) }

        repeat(threadCount) {
            executor.execute {
                try {
                    barrier.await()
                    mvpTestService.applyToMvpTest(memberId = it.toLong() + 1L, test.id!!)
                } catch (e: Exception) {
                    //logger 도입시 변경
                    println(e.message)
                }
            }
        }

        executor.awaitTermination(5, TimeUnit.SECONDS)

        memberTestRepository.findAll().size shouldBe recruitNum
    }
}