package com.team1.mvp_test.domain.report.dto

import jakarta.validation.constraints.Size

data class ReportRequest(
    @field:Size(min = 5, max = 50)
    val title: String,

    @field:Size(min = 5, max = 500)
    val body: String,

    @field:Size(min = 5, max = 500)
    val feedback: String,
)
