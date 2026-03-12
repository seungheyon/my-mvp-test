package com.team1.mvp_test.domain.report.service

import com.team1.mvp_test.common.error.ReportErrorMessage
import com.team1.mvp_test.common.exception.ModelNotFoundException
import com.team1.mvp_test.common.exception.NoPermissionException
import com.team1.mvp_test.domain.member.repository.MemberTestRepository
import com.team1.mvp_test.domain.mvptest.model.MvpTest
import com.team1.mvp_test.domain.report.dto.ReportRequest
import com.team1.mvp_test.domain.report.dto.ReportResponse
import com.team1.mvp_test.domain.report.model.Report
import com.team1.mvp_test.domain.report.model.ReportMedia
import com.team1.mvp_test.domain.report.model.ReportState
import com.team1.mvp_test.domain.report.repository.ReportMediaRepository
import com.team1.mvp_test.domain.report.repository.ReportRepository
import com.team1.mvp_test.domain.step.repository.StepRepository
import com.team1.mvp_test.infra.s3.s3service.S3Service
import com.team1.mvp_test.infra.s3.s3service.S3ServiceImpl
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class ReportService(
    private val reportRepository: ReportRepository,
    private val stepRepository: StepRepository,
    private val memberTestRepository: MemberTestRepository,
    private val reportMediaRepository: ReportMediaRepository,
    private val s3Service: S3Service
) {
    @Transactional
    fun getReport(memberId: Long, stepId: Long): ReportResponse? {
        val step = stepRepository.findByIdOrNull(stepId) ?: throw ModelNotFoundException("step", stepId)
        val memberTest = memberTestRepository.findByMemberIdAndTestId(memberId, step.mvpTest.id!!)
            ?: throw NoPermissionException(ReportErrorMessage.NO_PERMISSION.message)
        val report = reportRepository.findByStepIdAndMemberTestId(stepId, memberTest.id!!) ?: return null
        return ReportResponse.from(report)
    }

    @Transactional
    fun createReport(
        memberId: Long,
        stepId: Long,
        request: ReportRequest,
        mediaFile: MultipartFile
    ): ReportResponse {
        val step = stepRepository.findByIdOrNull(stepId) ?: throw ModelNotFoundException("step", stepId)
        val memberTest = memberTestRepository.findByMemberIdAndTestId(memberId, step.mvpTest.id!!)
            ?: throw NoPermissionException(ReportErrorMessage.NO_PERMISSION.message)
        check(
            !reportRepository.existsByStepAndMemberTest(
                step,
                memberTest
            )
        ) { ReportErrorMessage.ALREADY_REPORTED.message }
        checkDateCondition(step.mvpTest)
        if (mediaFile.isEmpty) throw IllegalStateException(ReportErrorMessage.REPORT_MEDIA_URL_NOT_EXIST.message)
        val fileUrl = mediaFile.let { s3Service.uploadReportFile(it) }
        val reportMedia = ReportMedia(mediaUrl = fileUrl)
            .let { reportMediaRepository.save(it) }
        val report = Report(
            title = request.title,
            body = request.body,
            feedback = request.feedback,
            memberTest = memberTest,
            step = step,
            reportMedia = mutableListOf(reportMedia)
        ).let { reportRepository.save(it) }

        return ReportResponse.from(report)
    }

    @Transactional
    fun updateReport(
        memberId: Long,
        reportId: Long,
        request: ReportRequest,
        mediaFile: MultipartFile
    ): ReportResponse {
        val report = reportRepository.findByIdOrNull(reportId) ?: throw ModelNotFoundException("report", reportId)

        if (report.memberTest.member.id != memberId) throw NoPermissionException(ReportErrorMessage.NOT_AUTHORIZED.message)

        check(report.state != ReportState.APPROVED) { ReportErrorMessage.ALREADY_APPROVED.message }

        checkDateCondition(report.step.mvpTest)

        report.updateReport(
            title = request.title,
            body = request.body,
            feedback = request.feedback,
        )
        if (mediaFile.isEmpty) throw IllegalStateException(ReportErrorMessage.REPORT_MEDIA_URL_NOT_EXIST.message)

        report.reportMedia.firstOrNull()?.let {
            s3Service.deleteFile(it.mediaUrl)
            reportMediaRepository.delete(it)
            report.clearReportMedia()
        }
        val fileUrl = s3Service.uploadReportFile(mediaFile)
        val reportMedia = ReportMedia(mediaUrl = fileUrl).apply { report.addReportMedia(this) }
        reportMediaRepository.save(reportMedia)

        return reportRepository.save(report).let { ReportResponse.from(it) }
    }

    @Transactional
    fun deleteReport(memberId: Long, reportId: Long) {
        val report = reportRepository.findByIdOrNull(reportId) ?: throw ModelNotFoundException("report", reportId)
        if (report.memberTest.member.id != memberId) throw NoPermissionException(ReportErrorMessage.NOT_AUTHORIZED.message)
        check(report.state != ReportState.APPROVED) { ReportErrorMessage.ALREADY_APPROVED.message }
        report.reportMedia.firstOrNull()?.let { media ->
            s3Service.deleteFile(media.mediaUrl)
            reportMediaRepository.delete(media)
        }
        reportRepository.delete(report)
    }

    @Transactional
    fun approveReport(enterpriseId: Long, reportId: Long): ReportResponse {
        val report = reportRepository.findByIdOrNull(reportId) ?: throw ModelNotFoundException("report", reportId)
        if (report.step.mvpTest.enterpriseId != enterpriseId) throw NoPermissionException(ReportErrorMessage.NO_PERMISSION.message)
        report.approve()
        return ReportResponse.from(report)
    }

    @Transactional
    fun rejectReport(enterpriseId: Long, reportId: Long, reason: String): ReportResponse {
        val report = reportRepository.findByIdOrNull(reportId) ?: throw ModelNotFoundException("report", reportId)
        if (report.step.mvpTest.enterpriseId != enterpriseId) throw NoPermissionException(ReportErrorMessage.NO_PERMISSION.message)
        report.reject(reason)
        return ReportResponse.from(report)
    }

    private fun checkDateCondition(test: MvpTest) {
        val currentDate = LocalDateTime.now()
        if (currentDate.isAfter(test.testEndDate) || currentDate.isBefore(test.testStartDate)) {
            throw IllegalArgumentException(ReportErrorMessage.NOT_TEST_DURATION.message)
        }
    }

}