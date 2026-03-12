package com.team1.mvp_test.batch.service

import com.team1.mvp_test.batch.reward.RewardSettlementJob
import org.springframework.stereotype.Service
import java.time.LocalDate


@Service
class BatchService(
    private val rewardSettlementJob: RewardSettlementJob
) {
    fun settleReward(date: LocalDate) {
        rewardSettlementJob.run(date)
    }
}