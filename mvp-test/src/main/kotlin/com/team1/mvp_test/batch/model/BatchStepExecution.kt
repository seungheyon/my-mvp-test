package com.team1.mvp_test.batch.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "batch_step_execution")
class BatchStepExecution(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "step_name")
    val stepName: String,

    @Column(name = "job_execution_id", nullable = false)
    val jobExecutionId: Long,

    @Column(name = "start_time")
    val startTime: LocalDateTime,

    @Column(name = "end_time")
    var endTime: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: BatchStepStatus,

    @Column(name = "exit_message")
    var exitMessage: String? = null

) {
    fun setStatusOnSuccess(endTime: LocalDateTime) {
        this.status = BatchStepStatus.COMPLETED
        this.endTime = endTime
    }

    fun setStatusOnError(endTime: LocalDateTime, errorMessage: String?) {
        this.status = BatchStepStatus.FAILED
        this.endTime = endTime
        this.exitMessage = errorMessage
    }
}

enum class BatchStepStatus {
    COMPLETED, FAILED, STARTED
}