package com.team1.mvp_test.domain.step.dto

import com.team1.mvp_test.domain.report.model.ReportState
import com.team1.mvp_test.domain.step.model.Step

data class StepListResponse(
    val stepId: Long,
    val title: String,
    val stepOrder: Int,
    val reward: Int,
    val reportState: ReportState?
) {
    companion object {
        fun from(step: Step, reportState: ReportState? = null): StepListResponse {
            return StepListResponse(
                stepId = step.id!!,
                title = step.title,
                stepOrder = step.stepOrder,
                reward = step.reward,
                reportState = reportState
            )
        }
    }
}
