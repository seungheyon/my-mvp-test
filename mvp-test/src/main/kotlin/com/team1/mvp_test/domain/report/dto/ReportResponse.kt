package com.team1.mvp_test.domain.report.dto

import com.team1.mvp_test.domain.report.model.Report

data class ReportResponse(
    val id: Long,
    val body: String,
    val title: String,
    val reportMedia: List<String>,
    val feedback: String,
    val state: String,
    val reason: String?,
) {
    companion object {
        fun from(report: Report): ReportResponse {
            return ReportResponse(
                id = report.id!!,
                body = report.body,
                title = report.title,
                reportMedia = report.reportMedia.map { it.mediaUrl },
                feedback = report.feedback,
                state = report.state.name,
                reason = report.reason
            )
        }
    }
}