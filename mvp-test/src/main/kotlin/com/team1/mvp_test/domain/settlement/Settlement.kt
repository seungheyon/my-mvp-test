package com.team1.mvp_test.domain.settlement

import com.team1.mvp_test.domain.mvptest.model.MvpTest
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "settlements")
class Settlement(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    var test: MvpTest,

    @Column(name = "date")
    val date: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: SettlementStatus = SettlementStatus.STARTED,

    @Column(name = "reward_amount")
    var rewardAmount: Int = 0


) {
    fun completeSettlement() {
        this.status = SettlementStatus.COMPLETED
    }

    fun addRewardAmount(amount: Int) {
        this.rewardAmount += amount
    }
}


enum class SettlementStatus {
    STARTED, COMPLETED
}