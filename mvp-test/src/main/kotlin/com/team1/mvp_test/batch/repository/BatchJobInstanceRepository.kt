package com.team1.mvp_test.batch.repository

import com.team1.mvp_test.batch.model.BatchJobInstance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BatchJobInstanceRepository : JpaRepository<BatchJobInstance, Long> {
    fun findByJobNameAndParameter(jobName: String, parameter: String): BatchJobInstance?
}