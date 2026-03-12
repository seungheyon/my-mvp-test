package com.team1.mvp_test.domain.step.dto

import com.team1.mvp_test.domain.report.model.ReportState


data class ReportStatusResponse(
    val memberId: Long,
    val memberEmail: String,
    val completionState: ReportState
)
