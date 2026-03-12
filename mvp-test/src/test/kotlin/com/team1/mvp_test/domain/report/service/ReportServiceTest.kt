package com.team1.mvp_test.domain.report.service

import com.team1.mvp_test.common.exception.NoPermissionException
import com.team1.mvp_test.domain.member.model.Member
import com.team1.mvp_test.domain.member.model.MemberState
import com.team1.mvp_test.domain.member.model.MemberTest
import com.team1.mvp_test.domain.member.model.Sex
import com.team1.mvp_test.domain.member.repository.MemberTestRepository
import com.team1.mvp_test.domain.mvptest.model.MvpTest
import com.team1.mvp_test.domain.mvptest.model.MvpTestState
import com.team1.mvp_test.domain.mvptest.model.RecruitType
import com.team1.mvp_test.domain.report.dto.ReportRequest
import com.team1.mvp_test.domain.report.model.Report
import com.team1.mvp_test.domain.report.model.ReportState
import com.team1.mvp_test.domain.report.repository.ReportMediaRepository
import com.team1.mvp_test.domain.report.repository.ReportRepository
import com.team1.mvp_test.domain.step.model.Step
import com.team1.mvp_test.domain.step.repository.StepRepository
import com.team1.mvp_test.infra.s3.s3service.S3ServiceImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDateTime


class ReportServiceTest : BehaviorSpec({

    val reportRepository = mockk<ReportRepository>(relaxed = true)
    val reportMediaRepository = mockk<ReportMediaRepository>(relaxed = true)
    val stepRepository = mockk<StepRepository>(relaxed = true)
    val memberTestRepository = mockk<MemberTestRepository>(relaxed = true)
    val s3Service = mockk<S3ServiceImpl>(relaxed = true)

    val reportService = ReportService(
        reportRepository = reportRepository,
        reportMediaRepository = reportMediaRepository,
        stepRepository = stepRepository,
        memberTestRepository = memberTestRepository,
        s3Service = s3Service
    )
    Given("파일 형식이 pdf가 아니면") {
        every { memberTestRepository.findByIdOrNull(any()) } returns memberTest
        every { stepRepository.findByIdOrNull(any()) } returns step
        every { s3Service.uploadReportFile(any()) } throws IllegalArgumentException()
        When("createReport 실행 시") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    reportService.createReport(MEMBER_ID, STEP_ID, request, invalidFile)
                }
            }
        }
    }

    Given("파일 확장자가 pdf이면서 파일 내용이 pdf가 아니라면") {
        every { memberTestRepository.findByIdOrNull(any()) } returns memberTest
        every { stepRepository.findByIdOrNull(any()) } returns step
        every { s3Service.uploadReportFile(any()) } throws IllegalArgumentException()
        When("createReport 실행 시") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    reportService.createReport(MEMBER_ID, STEP_ID, request, incorrectContentFile)
                }
            }
        }
    }


    Given("파일이 없으면 ") {
        every { memberTestRepository.findByIdOrNull(any()) } returns memberTest
        every { stepRepository.findByIdOrNull(any()) } returns step
        every { s3Service.uploadReportFile(any()) } throws IllegalArgumentException()
        When("createReport 실행 시 ") {
            Then("IllegalStateException 예외 발생") {
                shouldThrowExactly<IllegalStateException> {
                    reportService.createReport(MEMBER_ID, STEP_ID, request, emptyFile)
                }
            }
        }
    }

    Given("업로드 파일이 10MB 초과라면") {
        every { memberTestRepository.findByIdOrNull(any()) } returns memberTest
        every { stepRepository.findByIdOrNull(any()) } returns step
        every { s3Service.uploadReportFile(any()) } throws IllegalArgumentException()
        When("createReport 실행 시") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrowExactly<IllegalArgumentException> {
                    reportService.createReport(MEMBER_ID, STEP_ID, request, exceedMaxSizeFile)
                }
            }
        }
    }

    Given("파일 확장자가 pdf이면서 파일 내용이 pdf가 아니라면 ") {
        every { memberTestRepository.findByIdOrNull(any()) } returns memberTest
        every { stepRepository.findByIdOrNull(any()) } returns step
        every { reportRepository.findByIdOrNull(any()) } returns report
        every { reportMediaRepository.deleteAll(any()) } returns Unit
        every { s3Service.deleteFile(any()) } returns Unit
        every { s3Service.uploadReportFile(any()) } throws IllegalArgumentException()
        When("updateReport 실행") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrowExactly<IllegalArgumentException> {
                    reportService.updateReport(MEMBER_ID, REPORT_ID, request, incorrectContentFile)
                }
            }
        }
    }

    Given("파일 형식이 jpg, jpeg, png 이 아니면 ") {
        every { memberTestRepository.findByIdOrNull(any()) } returns memberTest
        every { stepRepository.findByIdOrNull(any()) } returns step
        every { reportRepository.findByIdOrNull(any()) } returns report
        every { reportMediaRepository.deleteAll(any()) } returns Unit
        every { s3Service.deleteFile(any()) } returns Unit
        every { s3Service.uploadReportFile(any()) } throws IllegalArgumentException()
        When("updateReport 실행") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrowExactly<IllegalArgumentException> {
                    reportService.updateReport(MEMBER_ID, REPORT_ID, request, invalidFile)
                }
            }
        }
    }



    Given("파일이 없으면") {
        every { memberTestRepository.findByIdOrNull(any()) } returns memberTest
        every { stepRepository.findByIdOrNull(any()) } returns step
        every { reportRepository.findByIdOrNull(any()) } returns report
        every { reportMediaRepository.deleteAll(any()) } returns Unit
        every { s3Service.deleteFile(any()) } returns Unit
        every { s3Service.uploadReportFile(any()) } throws IllegalArgumentException()
        When("updateReport 실행 ") {
            Then("IllegalStateException 예외 발생") {
                shouldThrowExactly<IllegalStateException> {
                    reportService.updateReport(MEMBER_ID, REPORT_ID, request, emptyFile)
                }
            }
        }
    }

    Given("업로드 파일이 10MB 초과일때") {
        every { memberTestRepository.findByIdOrNull(any()) } returns memberTest
        every { stepRepository.findByIdOrNull(any()) } returns step
        every { reportRepository.findByIdOrNull(any()) } returns report
        every { reportMediaRepository.deleteAll(any()) } returns Unit
        every { s3Service.deleteFile(any()) } returns Unit
        every { s3Service.uploadReportFile(any()) } throws IllegalArgumentException()
        When("updateReport 실행") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrowExactly<IllegalArgumentException> {
                    reportService.updateReport(MEMBER_ID, REPORT_ID, request, exceedMaxSizeFile)
                }
            }
        }
    }

    Given("작성 권한이 없는 사용자라면") {
        every { stepRepository.findByIdOrNull(any()) } returns step
        every { memberTestRepository.findByMemberIdAndTestId(any(), any()) } returns null
        When("createReport 실행 시") {
            Then("NoPermissionException 예외를 던진다") {
                shouldThrowExactly<NoPermissionException> {
                    reportService.createReport(MEMBER_ID, STEP_ID, request, validFile)
                }
            }
        }
    }
    Given("이미 Report 를 작성한 상태라면") {
        every { stepRepository.findByIdOrNull(any()) } returns step
        every { memberTestRepository.findByMemberIdAndTestId(any(), any()) } returns memberTest
        every { reportRepository.existsByStepAndMemberTest(any(), any()) } returns true
        When("createReport 실행 시") {
            Then("IllegalStateException 예외를 던진다") {
                shouldThrowExactly<IllegalStateException> {
                    reportService.createReport(MEMBER_ID, STEP_ID, request, validFile)
                }
            }
        }
    }

    Given("자신이 작성한 Report 아닌 경우") {
        every { reportRepository.findByIdOrNull(any()) } returns report
        When("updateReport 실행 시") {
            Then("NoPermissionException 예외를 던진다") {
                shouldThrowExactly<NoPermissionException> {
                    reportService.updateReport(OTHER_MEMBER_ID, REPORT_ID, request, validFile)
                }
            }
        }

        When("deleteReport 실행 시 ") {
            Then("NoPermissionException 예외를 던진다") {
                shouldThrowExactly<NoPermissionException> {
                    reportService.deleteReport(OTHER_MEMBER_ID, REPORT_ID)
                }
            }
        }
    }

    Given("Report 가 이미 승인된 경우") {
        every { reportRepository.findByIdOrNull(any()) } returns approvedReport
        When("updateReport 실행 시") {
            Then("IllegalStateException 예외를 던진다") {
                shouldThrowExactly<IllegalStateException> {
                    reportService.updateReport(MEMBER_ID, APPROVED_REPORT_ID, request, validFile)
                }
            }
        }
        When("deleteReport 실행 시") {
            Then("IllegalStateException 예외를 던진다") {
                shouldThrowExactly<IllegalStateException> {
                    reportService.deleteReport(MEMBER_ID, APPROVED_REPORT_ID)
                }
            }
        }
    }


    Given("권한이 없는 Report에 대해서") {
        every { reportRepository.findByIdOrNull(any()) } returns report
        val otherEnterpriseId = 2L
        When("approveReport 실행 시") {
            Then("NoPermissionException 예외를 던진다") {
                shouldThrowExactly<NoPermissionException> {
                    reportService.approveReport(otherEnterpriseId, REPORT_ID)
                }
            }
        }
    }

    Given("권한이 있는 Report에 대해서") {
        every { reportRepository.findByIdOrNull(any()) } returns report

        When("approveReport 실행 시") {
            val response = reportService.approveReport(ENTERPRISE_ID, REPORT_ID)
            Then("성공적으로 실행된다") {
                response.state shouldBe ReportState.APPROVED.name
            }
        }
    }


}) {
    companion object {
        private const val ENTERPRISE_ID = 1L
        private const val MEMBER_ID = 1L
        private const val OTHER_MEMBER_ID = 2L
        private const val TEST_ID = 1L
        private const val STEP_ID = 1L
        private const val REPORT_ID = 1L
        private const val APPROVED_REPORT_ID = 2L
        private val request = ReportRequest(
            title = "Test title",
            body = "Test body",
            feedback = "Test feedback",
        )
        private val mvpTest = MvpTest(
            id = TEST_ID,
            enterpriseId = ENTERPRISE_ID,
            mvpName = "test mvp name",
            recruitStartDate = LocalDateTime.of(2023, 5, 1, 12, 0),
            recruitEndDate = LocalDateTime.of(2024, 5, 5, 12, 0),
            testStartDate = LocalDateTime.of(2024, 5, 10, 12, 0),
            testEndDate = LocalDateTime.of(2025, 5, 15, 12, 0),
            mainImageUrl = "test mvp main url",
            mvpInfo = "test mvp info",
            mvpUrl = "test mvp url",
            rewardBudget = 1000,
            requirementMinAge = 0,
            requirementMaxAge = 200,
            requirementSex = Sex.MALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 100,
            state = MvpTestState.APPROVED
        )
        private val step = Step(
            id = STEP_ID,
            title = "Test step title",
            requirement = "Test requirement",
            guidelineUrl = "test guideline",
            stepOrder = 1,
            reward = 100,
            mvpTest = mvpTest
        )
        private val member = Member(
            id = MEMBER_ID,
            name = "test member name",
            email = "test@test.com",
            age = 20,
            sex = Sex.MALE,
            info = "test info",
            state = MemberState.ACTIVE
        )

        private val memberTest = MemberTest(
            member = member,
            test = mvpTest
        )

        private val report = Report(
            id = REPORT_ID,
            title = "Test report title",
            body = "Test report body",
            feedback = "Test feedback",
            state = ReportState.PENDING,
            reason = "Test report reason",
            step = step,
            memberTest = memberTest,
        )

        private val approvedReport = Report(
            id = APPROVED_REPORT_ID,
            title = "Test report title",
            body = "Test report body",
            feedback = "Test feedback",
            state = ReportState.APPROVED,
            reason = "Test report reason",
            step = step,
            memberTest = memberTest,
        )

        private val validFile = MockMultipartFile(
            "validFile1",
            "test1.pdf",
            "pdf",
            ByteArray(1)
        )

        private val invalidFile = MockMultipartFile(
            "invalidFile",
            "test3.pdf",
            "jpg",
            ByteArray(1)
        )
        private val emptyFile = MockMultipartFile(
            "emptyFile",
            "test4.pdf",
            "pdf",
            ByteArray(0)
        )

        private val exceedMaxSizeFile = MockMultipartFile(
            "exceedMaxSizeFile",
            "exceedMaxSize.pdf",
            "pdf",
            ByteArray(16)
        )

        private val incorrectContentFile = MockMultipartFile(
            "incorrectContentFile",
            "incorrect.jpg",
            "pdf",
            ByteArray(1)
        )

    }
}