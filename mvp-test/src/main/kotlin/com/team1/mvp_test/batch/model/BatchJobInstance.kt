package com.team1.mvp_test.batch.model

import jakarta.persistence.*

@Entity
@Table(name = "batch_job_instance")
class BatchJobInstance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "job_name")
    val jobName: String,

    @Column(name = "batch_parameter")
    val parameter: String
)