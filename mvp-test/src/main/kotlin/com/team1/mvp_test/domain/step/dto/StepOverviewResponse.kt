package com.team1.mvp_test.domain.step.dto

data class StepOverviewResponse(
    val completionRate: Int,
    val reportStatusList: List<ReportStatusResponse>
)
