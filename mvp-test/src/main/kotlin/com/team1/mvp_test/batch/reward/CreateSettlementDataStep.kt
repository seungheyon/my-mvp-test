package com.team1.mvp_test.batch.reward

import com.team1.mvp_test.batch.Step
import com.team1.mvp_test.batch.repository.BatchStepExecutionRepository
import com.team1.mvp_test.domain.mvptest.repository.MvpTestRepository
import com.team1.mvp_test.domain.settlement.Settlement
import com.team1.mvp_test.domain.settlement.SettlementRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class CreateSettlementDataStep(
    override val batchStepExecutionRepository: BatchStepExecutionRepository,
    private val mvpTestRepository: MvpTestRepository,
    private val settlementRepository: SettlementRepository

) : Step() {

    override val name = "CreateSettlementData"

    @Transactional
    override fun execute(date: LocalDate) {
        val unSettledTests = mvpTestRepository.findAllUnsettledMvpTests(date)
        for (test in unSettledTests) {
            settlementRepository.save(
                Settlement(
                    test = test,
                    date = date,
                )
            )
        }
    }
}