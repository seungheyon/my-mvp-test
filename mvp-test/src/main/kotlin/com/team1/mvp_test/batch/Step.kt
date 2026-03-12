package com.team1.mvp_test.batch

import com.team1.mvp_test.batch.model.BatchStepExecution
import com.team1.mvp_test.batch.model.BatchStepStatus
import com.team1.mvp_test.batch.repository.BatchStepExecutionRepository
import java.time.LocalDate
import java.time.LocalDateTime

abstract class Step {
    abstract val batchStepExecutionRepository: BatchStepExecutionRepository

    fun stepExecute(jobExecutionId: Long, date: LocalDate) {
        val stepExecution = batchStepExecutionRepository.saveAndFlush(
            BatchStepExecution(
                stepName = name,
                jobExecutionId = jobExecutionId,
                startTime = LocalDateTime.now(),
                status = BatchStepStatus.STARTED
            )
        )
        runCatching {
            execute(date)
        }.onSuccess {
            stepExecution.setStatusOnSuccess(LocalDateTime.now())
            batchStepExecutionRepository.save(stepExecution)
        }.onFailure { error ->
            stepExecution.setStatusOnError(LocalDateTime.now(), error.message)
            batchStepExecutionRepository.save(stepExecution)
            throw error
        }

    }

    abstract val name: String
    abstract fun execute(date: LocalDate)

}