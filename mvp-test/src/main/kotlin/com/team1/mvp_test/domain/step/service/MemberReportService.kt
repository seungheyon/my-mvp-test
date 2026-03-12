package com.team1.mvp_test.domain.step.service

import com.team1.mvp_test.domain.member.repository.MemberTestRepository
import com.team1.mvp_test.domain.report.model.ReportState
import com.team1.mvp_test.domain.report.repository.ReportRepository
import com.team1.mvp_test.domain.step.dto.ReportStatusResponse
import com.team1.mvp_test.domain.step.model.Step
import org.springframework.stereotype.Component

@Component
class MemberReportService(
    private val memberTestRepository : MemberTestRepository,
    private val reportRepository: ReportRepository
) {

    fun getMappedReport(step: Step):List<ReportStatusResponse> {
        val members = memberTestRepository.findAllAndMemberByTestId(step.mvpTest.id!!)
        val reports = reportRepository.findAllByStepId(step.id!!)

        val reportMap = reports.associateBy({ it.memberTest.member.id }, { it.state })
        val reportStatusResponse = members.map { memberTest ->
            val reportState = reportMap[memberTest.member.id] ?: ReportState.MISSING
            ReportStatusResponse(
                memberId = memberTest.member.id!!,
                memberEmail = memberTest.member.email,
                completionState = reportState
            )
        }
        return reportStatusResponse
    }


}