package com.team1.mvp_test.domain.step.service

import com.team1.mvp_test.common.error.MvpTestErrorMessage
import com.team1.mvp_test.common.error.StepErrorMessage
import com.team1.mvp_test.common.exception.ModelNotFoundException
import com.team1.mvp_test.common.exception.NoPermissionException
import com.team1.mvp_test.domain.member.repository.MemberRepository
import com.team1.mvp_test.domain.member.repository.MemberTestRepository
import com.team1.mvp_test.domain.mvptest.model.MvpTestState
import com.team1.mvp_test.domain.mvptest.repository.MvpTestRepository
import com.team1.mvp_test.domain.report.model.ReportState
import com.team1.mvp_test.domain.report.repository.ReportRepository
import com.team1.mvp_test.domain.step.dto.*
import com.team1.mvp_test.domain.step.model.Step
import com.team1.mvp_test.domain.step.repository.StepRepository
import com.team1.mvp_test.infra.s3.s3service.S3Service
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class StepService(
    private val stepRepository: StepRepository,
    private val mvpTestRepository: MvpTestRepository,
    private val s3Service: S3Service,
    private val memberReportService: MemberReportService,
    private val memberRepository: MemberRepository,
    private val reportRepository: ReportRepository,
    private val memberTestRepository: MemberTestRepository,
) {


    fun getStepList(memberId: Long?, testId: Long): List<StepListResponse> {
        val mvpTest = mvpTestRepository.findByIdOrNull(testId)
            ?: throw ModelNotFoundException("MvpTest", testId)
        if (memberId == null) return stepRepository.findAllByMvpTestIdOrderByStepOrder(testId)
            .map { StepListResponse.from(it) }
        else {
            val member = memberRepository.findByIdOrNull(memberId)
                ?: throw ModelNotFoundException("Member", memberId)
            val memberTest = memberTestRepository.findByMemberIdAndTestId(member.id!!, testId)
                ?: throw ModelNotFoundException("MemberTest", "$memberId, $testId")
            return stepRepository.findAllByMvpTestIdOrderByStepOrder(testId).map {
                val report = reportRepository.findByStepIdAndMemberTestId(it.id!!, memberTest.id!!)
                val state = report?.state ?: ReportState.MISSING
                StepListResponse.from(it, state)
            }
        }
    }

    @Transactional
    fun createStep(
        enterpriseId: Long,
        testId: Long,
        request: CreateStepRequest,
        guidelineFile: MultipartFile?
    ): StepResponse {
        val mvpTest = mvpTestRepository.findByIdOrNull(testId)
            ?: throw ModelNotFoundException("MvpTest", testId)
        if (mvpTest.state != MvpTestState.APPROVED) throw NoPermissionException(StepErrorMessage.NOT_APPROVED_TEST.message)
        if (mvpTest.enterpriseId != enterpriseId) throw NoPermissionException(MvpTestErrorMessage.NOT_AUTHORIZED.message)
        val maxOrder = stepRepository.findMaxOrderByTestId(testId)
        val file = guidelineFile?.let { s3Service.uploadStepFile(it) }
        return Step(
            title = request.title,
            requirement = request.requirement,
            guidelineUrl = file,
            reward = request.reward,
            stepOrder = maxOrder + 1,
            mvpTest = mvpTest
        ).let { stepRepository.save(it) }
            .let { StepResponse.from(it) }
    }

    fun getStepById(stepId: Long): StepResponse {
        val step = stepRepository.findByIdOrNull(stepId)
            ?: throw ModelNotFoundException("Step", stepId)
        return StepResponse.from(step)
    }

    @Transactional
    fun updateStep(
        enterpriseId: Long,
        stepId: Long,
        request: UpdateStepRequest,
        guidelineFile: MultipartFile?
    ): StepResponse {
        val step = stepRepository.findByIdOrNull(stepId)
            ?: throw ModelNotFoundException("Step", stepId)
        if (step.mvpTest.enterpriseId != enterpriseId) throw NoPermissionException(MvpTestErrorMessage.NOT_AUTHORIZED.message)
        step.guidelineUrl?.let { s3Service.deleteFile(it) }
        val file = guidelineFile?.let { s3Service.uploadStepFile(it) }
        step.updateStep(
            title = request.title,
            requirement = request.requirement,
            guidelineUrl = file,
        )
        return StepResponse.from(step)
    }

    @Transactional
    fun deleteStep(enterpriseId: Long, stepId: Long) {
        val step = stepRepository.findByIdOrNull(stepId)
            ?: throw ModelNotFoundException("Step", stepId)
        if (step.mvpTest.enterpriseId != enterpriseId) throw NoPermissionException(MvpTestErrorMessage.NOT_AUTHORIZED.message)
        step.guidelineUrl?.let { s3Service.deleteFile(it) }
        stepRepository.delete(step)
    }

    fun getStepOverview(enterpriseId: Long, stepId: Long): StepOverviewResponse {
        val step = stepRepository.findByIdOrNull(stepId)
            ?: throw ModelNotFoundException("Step", stepId)
        if (enterpriseId != step.mvpTest.enterpriseId)
            throw NoPermissionException(MvpTestErrorMessage.NOT_AUTHORIZED.message)

        val reportStateList = memberReportService.getMappedReport(step)

        return StepOverviewResponse(
            completionRate = reportStateList.calculateProgressPercentage(),
            reportStatusList = reportStateList
        )
    }

    fun List<ReportStatusResponse>.calculateProgressPercentage(): Int {
        val approvedCount = this.count { it.completionState == ReportState.APPROVED }
        val totalCount = this.size
        return if (totalCount > 0) {
            ((approvedCount.toDouble() / totalCount) * 100).toInt()
        } else {
            0
        }
    }
}
