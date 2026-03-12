package com.team1.mvp_test.batch.repository

import com.team1.mvp_test.batch.model.BatchJobExecution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BatchJobExecutionRepository : JpaRepository<BatchJobExecution, Long> {
    fun findByJobInstanceId(batchJobExecutionId: Long): BatchJobExecution?
}