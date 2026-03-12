package com.team1.mvp_test.batch.reward

import com.team1.mvp_test.batch.Job
import com.team1.mvp_test.batch.repository.BatchJobExecutionRepository
import com.team1.mvp_test.batch.repository.BatchJobInstanceRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class RewardSettlementJob(
    override val batchJobInstanceRepository: BatchJobInstanceRepository,
    override val batchJobExecutionRepository: BatchJobExecutionRepository,
    private val createSettlementDataStep: CreateSettlementDataStep,
    private val payMemberRewardStep: PayMemberRewardStep
) : Job() {
    override val name = "RewardSettlementJob"

    @Transactional
    override fun execute(jobExecutionId: Long, date: LocalDate) {
        createSettlementDataStep.stepExecute(jobExecutionId, date)
        payMemberRewardStep.stepExecute(jobExecutionId, date)
    }

}