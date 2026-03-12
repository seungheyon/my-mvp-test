package com.team1.mvp_test.domain.settlement

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface SettlementRepository : JpaRepository<Settlement, Long> {
    fun findAllByDateAndStatus(date: LocalDate, status: SettlementStatus): List<Settlement>
}