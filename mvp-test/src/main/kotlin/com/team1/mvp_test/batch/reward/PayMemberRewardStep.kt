package com.team1.mvp_test.batch.reward

import com.team1.mvp_test.batch.Step
import com.team1.mvp_test.batch.repository.BatchStepExecutionRepository
import com.team1.mvp_test.domain.member.model.MemberReward
import com.team1.mvp_test.domain.member.repository.MemberRewardRepository
import com.team1.mvp_test.domain.member.repository.MemberTestRepository
import com.team1.mvp_test.domain.report.model.ReportState
import com.team1.mvp_test.domain.report.repository.ReportRepository
import com.team1.mvp_test.domain.settlement.SettlementRepository
import com.team1.mvp_test.domain.settlement.SettlementStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class PayMemberRewardStep(
    override val batchStepExecutionRepository: BatchStepExecutionRepository,
    private val settlementRepository: SettlementRepository,
    private val memberTestRepository: MemberTestRepository,
    private val reportRepository: ReportRepository,
    private val memberRewardRepository: MemberRewardRepository,

    ) : Step() {
    override val name = "PayMemberReward"

    @Transactional
    override fun execute(date: LocalDate) {
        val settlements = settlementRepository.findAllByDateAndStatus(date, SettlementStatus.STARTED)
        for (settlement in settlements) {
            val memberTests = memberTestRepository.findAllByTestId(settlement.test.id!!)
            for (memberTest in memberTests) {
                reportRepository.findAllByMemberTestAndState(memberTest, ReportState.APPROVED).forEach {
                    val message = createRewardMessage(it.step.mvpTest.mvpName, it.step.title)
                    val reward = it.step.reward
                    memberRewardRepository.save(
                        MemberReward(
                            memberId = it.memberTest.member.id!!,
                            amount = reward,
                            message = message
                        )
                    )
                    memberTest.member.settleReward(reward)
                    settlement.addRewardAmount(reward)
                }
            }
            settlement.test.settlementDate = date
        }
    }

    private fun createRewardMessage(testTitle: String, stepTitle: String): String {
        return "[Test] : $testTitle, [Step] : $stepTitle"
    }
}