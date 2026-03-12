package com.team1.mvp_test.domain.step.repository

import com.team1.mvp_test.domain.step.model.Step
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface StepRepository : JpaRepository<Step, Long> {

    @Query("SELECT COALESCE(MAX(s.stepOrder), 0) FROM Step s WHERE s.mvpTest.id = :testId")
    fun findMaxOrderByTestId(@Param("testId") testId: Long): Int

    fun findAllByMvpTestIdOrderByStepOrder(testId: Long): List<Step>

}