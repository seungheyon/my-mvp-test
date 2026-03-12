package com.team1.mvp_test.domain.batch

import com.team1.mvp_test.batch.exception.BatchJobAlreadyDoneException
import com.team1.mvp_test.batch.model.BatchJobExecution
import com.team1.mvp_test.batch.model.BatchJobInstance
import com.team1.mvp_test.batch.model.BatchJobStatus
import com.team1.mvp_test.batch.repository.BatchJobExecutionRepository
import com.team1.mvp_test.batch.repository.BatchJobInstanceRepository
import com.team1.mvp_test.batch.reward.CreateSettlementDataStep
import com.team1.mvp_test.batch.reward.PayMemberRewardStep
import com.team1.mvp_test.batch.reward.RewardSettlementJob
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import java.time.LocalDate
import java.time.LocalDateTime

class RewardSettlementJobUnitTest : BehaviorSpec({
    val batchJobInstanceRepository = mockk<BatchJobInstanceRepository>()
    val batchJobExecutionRepository = mockk<BatchJobExecutionRepository>()
    val payMemberRewardStep = mockk<PayMemberRewardStep>()
    val createSettlementDataStep = mockk<CreateSettlementDataStep>()


    val rewardSettlementJob = RewardSettlementJob(
        batchJobInstanceRepository = batchJobInstanceRepository,
        batchJobExecutionRepository = batchJobExecutionRepository,
        payMemberRewardStep = payMemberRewardStep,
        createSettlementDataStep = createSettlementDataStep
    )


    Given("JobExecution 기록이 존재하며 상태가 COMPLETED인 경우") {
        val jobInstance = BatchJobInstance(
            id = 1L,
            jobName = rewardSettlementJob.name,
            parameter = LocalDate.of(2024, 10, 1).toString()
        )
        val jobExecution = BatchJobExecution(
            id = 1L,
            jobInstanceId = 1L,
            status = BatchJobStatus.COMPLETED,
        )
        every { batchJobInstanceRepository.findByJobNameAndParameter(any(), any()) } returns jobInstance
        every { batchJobExecutionRepository.findByJobInstanceId(any()) } returns jobExecution
        When("RewardSettlementJob의 run 실행 시") {
            Then("BatchJobAlreadyDoneException 발생") {
                shouldThrow<BatchJobAlreadyDoneException> {
                    rewardSettlementJob.run(LocalDate.of(2024, 10, 1))
                }
            }
        }
    }

    Given("BatchJobInstance가 존재하지 않는 경우") {
        every { batchJobInstanceRepository.findByJobNameAndParameter(any(), any()) } returns null
        every { batchJobInstanceRepository.save(any()) } returns BatchJobInstance(
            id = 1L,
            jobName = rewardSettlementJob.name,
            parameter = LocalDate.of(2024, 10, 1).toString(),
        )
        every { batchJobExecutionRepository.saveAndFlush(any()) } returns BatchJobExecution(
            id = 1L,
            jobInstanceId = 1L,
            status = BatchJobStatus.STARTED
        )
        every { batchJobExecutionRepository.save(any()) } returns BatchJobExecution(
            id = 1L,
            jobInstanceId = 1L,
            startTime = LocalDateTime.of(2024, 10, 1, 7, 10, 1),
            endTime = LocalDateTime.of(2024, 10, 1, 7, 10, 2),
            status = BatchJobStatus.COMPLETED
        )
        every { createSettlementDataStep.execute(any()) } just Runs
        every { payMemberRewardStep.execute(any()) } just Runs
        When("RewardSettlementJob의 run 실행 시") {
            rewardSettlementJob.run(LocalDate.of(2024, 10, 1))
            Then("JobInstance와 JobExecution 생성") {
                verify(exactly = 1) { batchJobInstanceRepository.save(any()) }
                verify(exactly = 1) { batchJobExecutionRepository.save(any()) }
                verify(exactly = 1) { batchJobExecutionRepository.saveAndFlush(any()) }
            }
        }
    }
})