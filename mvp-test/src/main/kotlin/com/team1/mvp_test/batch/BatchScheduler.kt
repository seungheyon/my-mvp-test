package com.team1.mvp_test.batch

import com.team1.mvp_test.batch.service.BatchService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class BatchScheduler(
    private val batchService: BatchService
) {

    @Scheduled(cron = "0 0 0 * * *")
    fun executeRewardSettle() {
        val date = LocalDate.now()
        batchService.settleReward(date)
    }
}