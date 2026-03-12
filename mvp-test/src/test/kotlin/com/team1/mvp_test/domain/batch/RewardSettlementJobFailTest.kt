package com.team1.mvp_test.domain.batch

import com.team1.mvp_test.batch.model.BatchJobStatus
import com.team1.mvp_test.batch.model.BatchStepStatus
import com.team1.mvp_test.batch.repository.BatchJobExecutionRepository
import com.team1.mvp_test.batch.repository.BatchJobInstanceRepository
import com.team1.mvp_test.batch.repository.BatchStepExecutionRepository
import com.team1.mvp_test.batch.reward.CreateSettlementDataStep
import com.team1.mvp_test.batch.reward.PayMemberRewardStep
import com.team1.mvp_test.batch.reward.RewardSettlementJob
import com.team1.mvp_test.domain.member.repository.MemberRewardRepository
import com.team1.mvp_test.domain.member.repository.MemberTestRepository
import com.team1.mvp_test.domain.mvptest.repository.MvpTestRepository
import com.team1.mvp_test.domain.report.repository.ReportRepository
import com.team1.mvp_test.domain.settlement.SettlementRepository
import com.team1.mvp_test.infra.querydsl.QueryDslConfig
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(value = [QueryDslConfig::class])
class RewardSettlementJobFailTest @Autowired constructor(
    private val batchJobInstanceRepository: BatchJobInstanceRepository,
    private val batchJobExecutionRepository: BatchJobExecutionRepository,
    private val batchStepExecutionRepository: BatchStepExecutionRepository,
) {
    private val mvpTestRepository = mockk<MvpTestRepository>()
    private val settlementRepository = mockk<SettlementRepository>()
    private val memberTestRepository = mockk<MemberTestRepository>()
    private val memberRewardRepository = mockk<MemberRewardRepository>()
    private val reportRepository = mockk<ReportRepository>()

    private final val createSettlementDataStep = CreateSettlementDataStep(
        batchStepExecutionRepository = batchStepExecutionRepository,
        mvpTestRepository = mvpTestRepository,
        settlementRepository = settlementRepository,
    )
    private final val payMemberRewardStep = PayMemberRewardStep(
        batchStepExecutionRepository = batchStepExecutionRepository,
        memberTestRepository = memberTestRepository,
        reportRepository = reportRepository,
        settlementRepository = settlementRepository,
        memberRewardRepository = memberRewardRepository,
    )

    val rewardSettlementJob = RewardSettlementJob(
        batchJobInstanceRepository = batchJobInstanceRepository,
        batchJobExecutionRepository = batchJobExecutionRepository,
        payMemberRewardStep = payMemberRewardStep,
        createSettlementDataStep = createSettlementDataStep
    )

    @AfterEach
    fun cleanUp() {
        batchJobInstanceRepository.deleteAll()
        batchJobExecutionRepository.deleteAll()
        batchStepExecutionRepository.deleteAll()

    }

    @Test
    fun `첫 번째 Step에서 오류가 발생한 경우 JobExecution 및 stepExecution 상태 확인`() {
        // given
        every { mvpTestRepository.findAllUnsettledMvpTests(any()) } throws Exception("error message")
        val date = LocalDate.of(2024, 10, 1)
        // when
        rewardSettlementJob.run(date)

        // then
        val jobInstance = batchJobInstanceRepository.findAll().first()
        val jobExecution = batchJobExecutionRepository.findAll().first()
        val stepExecution = batchStepExecutionRepository.findAll().first()

        jobInstance shouldNotBe null
        jobInstance!!.jobName shouldBe rewardSettlementJob.name
        jobInstance.parameter shouldBe date.toString()

        jobExecution shouldNotBe null
        jobExecution!!.status shouldBe BatchJobStatus.FAILED
        jobExecution.startTime shouldNotBe null
        jobExecution.endTime shouldNotBe null
        jobExecution.exitMessage shouldBe "error message"

        stepExecution shouldNotBe null
        stepExecution!!.status shouldBe BatchStepStatus.FAILED
        stepExecution.startTime shouldNotBe null
        stepExecution.endTime shouldNotBe null
        stepExecution.exitMessage shouldBe "error message"

    }

    @Test
    fun `두 번째 Step에서 오류가 발생한 경우 JobExecution 및 stepExecution 상태 확인`() {
        // given
        every { mvpTestRepository.findAllUnsettledMvpTests(any()) } returns emptyList()
        every { settlementRepository.findAllByDateAndStatus(any(), any()) } throws Exception("error message")
        val date = LocalDate.of(2024, 10, 1)
        // when
        rewardSettlementJob.run(date)

        // then
        val jobInstance = batchJobInstanceRepository.findAll().first()
        val jobExecution = batchJobExecutionRepository.findAll().first()
        val stepExecutions = batchStepExecutionRepository.findAll()
        val stepExecution1 = stepExecutions.first()
        val stepExecution2 = stepExecutions.last()


        jobInstance shouldNotBe null
        jobInstance!!.jobName shouldBe rewardSettlementJob.name
        jobInstance.parameter shouldBe date.toString()

        jobExecution shouldNotBe null
        jobExecution!!.status shouldBe BatchJobStatus.FAILED
        jobExecution.startTime shouldNotBe null
        jobExecution.endTime shouldNotBe null
        jobExecution.exitMessage shouldBe "error message"

        stepExecution1 shouldNotBe null
        stepExecution1!!.status shouldBe BatchStepStatus.COMPLETED
        stepExecution1.startTime shouldNotBe null
        stepExecution1.endTime shouldNotBe null
        stepExecution1.exitMessage shouldBe null

        stepExecution2 shouldNotBe null
        stepExecution2!!.status shouldBe BatchStepStatus.FAILED
        stepExecution2.startTime shouldNotBe null
        stepExecution2.endTime shouldNotBe null
        stepExecution2.exitMessage shouldBe "error message"

    }

}