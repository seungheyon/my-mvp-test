package com.team1.mvp_test.batch.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "batch_job_execution")
class BatchJobExecution(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "job_instance_id")
    var jobInstanceId: Long,

    @Column(name = "start_time")
    var startTime: LocalDateTime? = null,

    @Column(name = "end_time")
    var endTime: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: BatchJobStatus,

    @Column(name = "exit_message")
    var exitMessage: String? = null,

    ) {
    fun setStatusOnSuccess(endTime: LocalDateTime) {
        this.status = BatchJobStatus.COMPLETED
        this.endTime = endTime
    }

    fun setStatusOnError(endTime: LocalDateTime, errorMessage: String?) {
        this.status = BatchJobStatus.FAILED
        this.endTime = endTime
        this.exitMessage = errorMessage
    }
}

enum class BatchJobStatus {
    COMPLETED, FAILED, STARTED,
}
