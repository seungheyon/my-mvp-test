package com.team1.mvp_test.batch.repository

import com.team1.mvp_test.batch.model.BatchStepExecution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BatchStepExecutionRepository : JpaRepository<BatchStepExecution, Long> {
}