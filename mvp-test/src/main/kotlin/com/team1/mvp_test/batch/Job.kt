package com.team1.mvp_test.batch

import com.team1.mvp_test.batch.exception.BatchJobAlreadyDoneException
import com.team1.mvp_test.batch.model.BatchJobExecution
import com.team1.mvp_test.batch.model.BatchJobInstance
import com.team1.mvp_test.batch.model.BatchJobStatus
import com.team1.mvp_test.batch.repository.BatchJobExecutionRepository
import com.team1.mvp_test.batch.repository.BatchJobInstanceRepository
import java.time.LocalDate
import java.time.LocalDateTime

abstract class Job {
    abstract val batchJobInstanceRepository: BatchJobInstanceRepository
    abstract val batchJobExecutionRepository: BatchJobExecutionRepository

    fun run(date: LocalDate) {
        val jobParameter = date.toString()
        checkIsRunnable(jobParameter)
        val jobInstance = createJobInstanceIfAbsent(jobParameter)
        jobExecute(jobInstance.id!!, date)
    }

    private fun checkIsRunnable(parameter: String) {
        val jobInstance = batchJobInstanceRepository.findByJobNameAndParameter(name, parameter) ?: return
        val jobExecution = batchJobExecutionRepository.findByJobInstanceId(jobInstance.id!!) ?: return
        if (jobExecution.status == BatchJobStatus.COMPLETED) throw BatchJobAlreadyDoneException(name, parameter)
    }

    private fun createJobInstanceIfAbsent(parameter: String): BatchJobInstance {
        return batchJobInstanceRepository.findByJobNameAndParameter(name, parameter) ?: run {
            batchJobInstanceRepository.save(
                BatchJobInstance(
                    jobName = name,
                    parameter = parameter,
                )
            )
        }
    }

    private fun jobExecute(jobInstanceId: Long, date: LocalDate) {
        val jobExecution = batchJobExecutionRepository.saveAndFlush(
            BatchJobExecution(
                jobInstanceId = jobInstanceId,
                startTime = LocalDateTime.now(),
                status = BatchJobStatus.STARTED
            )
        )
        runCatching {
            execute(jobExecution.id!!, date)
        }.onSuccess {
            jobExecution.setStatusOnSuccess(LocalDateTime.now())
        }.onFailure { error ->
            jobExecution.setStatusOnError(LocalDateTime.now(), error.message)
        }
        batchJobExecutionRepository.save(jobExecution)
    }

    abstract val name: String
    abstract fun execute(jobExecutionId: Long, date: LocalDate)
}