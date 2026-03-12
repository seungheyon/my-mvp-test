package com.team1.mvp_test.domain.step.service

import com.team1.mvp_test.common.exception.ModelNotFoundException
import com.team1.mvp_test.common.exception.NoPermissionException
import com.team1.mvp_test.domain.member.model.Sex
import com.team1.mvp_test.domain.member.repository.MemberRepository
import com.team1.mvp_test.domain.member.repository.MemberTestRepository
import com.team1.mvp_test.domain.mvptest.model.MvpTest
import com.team1.mvp_test.domain.mvptest.model.MvpTestState
import com.team1.mvp_test.domain.mvptest.model.RecruitType
import com.team1.mvp_test.domain.mvptest.repository.MvpTestRepository
import com.team1.mvp_test.domain.report.repository.ReportRepository
import com.team1.mvp_test.domain.step.dto.CreateStepRequest
import com.team1.mvp_test.domain.step.dto.UpdateStepRequest
import com.team1.mvp_test.domain.step.model.Step
import com.team1.mvp_test.domain.step.repository.StepRepository
import com.team1.mvp_test.infra.s3.s3service.S3ServiceImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDateTime


class StepServiceTest : BehaviorSpec({

    val stepRepository = mockk<StepRepository>()
    val mvpTestRepository = mockk<MvpTestRepository>()
    val s3Service = mockk<S3ServiceImpl>()
    val memberReportService = mockk<MemberReportService>()
    val memberRepository = mockk<MemberRepository>()
    val reportRepository = mockk<ReportRepository>()
    val memberTestRepository = mockk<MemberTestRepository>()

    val stepService = StepService(
        stepRepository = stepRepository,
        mvpTestRepository = mvpTestRepository,
        s3Service = s3Service,
        memberReportService = memberReportService,
        memberRepository = memberRepository,
        reportRepository = reportRepository,
        memberTestRepository = memberTestRepository
    )
    Given("파일 형식이  pdf가 아니면") {
        every { mvpTestRepository.findByIdOrNull(any()) } returns mvpTest
        every { s3Service.uploadStepFile(any()) } throws IllegalArgumentException()
        every { stepRepository.findMaxOrderByTestId(any()) } returns 1
        When("createStep 실행 시") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    stepService.createStep(ENTERPRISE_ID, TEST_ID, createStepRequest, invalidFile)
                }
            }
        }
    }

    Given("파일 확장자가 pdf이면서 파일 내용은 pdf가 아닐 때") {
        every { mvpTestRepository.findByIdOrNull(any()) } returns mvpTest
        every { s3Service.uploadStepFile(any()) } throws IllegalArgumentException()
        every { stepRepository.findMaxOrderByTestId(any()) } returns 1
        When("createStep 실행 시") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    stepService.createStep(ENTERPRISE_ID, TEST_ID, createStepRequest, incorrectContentFile)
                }
            }
        }
    }


    Given("업로드 파일이 10MB 초과일때") {
        every { mvpTestRepository.findByIdOrNull(any()) } returns mvpTest
        every { s3Service.uploadStepFile(any()) } throws IllegalArgumentException()
        every { stepRepository.findMaxOrderByTestId(any()) } returns 1
        When("createStep 실행 시") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    stepService.createStep(ENTERPRISE_ID, TEST_ID, createStepRequest, exceedMaxSizeFile)
                }
            }
        }
    }

    Given("파일 형식이 pdf가 아닐때") {
        every { s3Service.uploadStepFile(any()) } throws IllegalArgumentException()
        every { stepRepository.findByIdOrNull(any()) } returns step
        every { stepRepository.findMaxOrderByTestId(any()) } returns MAX_ORDER
        every { s3Service.deleteFile(any()) } returns Unit
        When("updateStep 실행 시") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    stepService.updateStep(ENTERPRISE_ID, STEP_ID, updateStepRequest, invalidFile)
                }

            }
        }
    }

    Given("파일 확장자가 pdf이면서 파일 내용은 pdf가 아니라면") {
        every { mvpTestRepository.findByIdOrNull(any()) } returns mvpTest
        every { s3Service.uploadStepFile(any()) } throws IllegalArgumentException()
        every { stepRepository.findMaxOrderByTestId(any()) } returns 1
        When("updateStep 실행 시") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    stepService.updateStep(ENTERPRISE_ID, TEST_ID, updateStepRequest, incorrectContentFile)
                }
            }
        }
    }


    Given("업로드 파일이 10MB 초과라면") {
        every { s3Service.uploadStepFile(any()) } throws IllegalArgumentException()
        every { stepRepository.findByIdOrNull(any()) } returns step
        every { stepRepository.findMaxOrderByTestId(any()) } returns MAX_ORDER
        every { s3Service.deleteFile(any()) } returns Unit
        When("updateStep 실행 시") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    stepService.updateStep(ENTERPRISE_ID, STEP_ID, updateStepRequest, exceedMaxSizeFile)
                }
            }
        }
    }

    Given("존재하지 않는 테스트에 대해서") {
        every { mvpTestRepository.findByIdOrNull(any()) } returns null
        When("createStep 실행 시") {
            Then("ModelNotFoundException 예외를 던진다") {
                shouldThrow<ModelNotFoundException> {
                    stepService.createStep(ENTERPRISE_ID, TEST_ID, createStepRequest, guidelineFile)
                }
            }
        }
    }

    Given("권한이 없는 테스트에 대해서") {
        every { mvpTestRepository.findByIdOrNull(any()) } returns mvpTest
        every { stepRepository.findByIdOrNull(any()) } returns step
        val otherEnterpriseId = 2L
        When("createStep 실행 시") {
            Then("NoPermissionException 예외를 던진다") {
                shouldThrow<NoPermissionException> {
                    stepService.createStep(otherEnterpriseId, TEST_ID, createStepRequest, guidelineFile)
                }
            }
        }
        When("updateStep 실행 시") {
            Then("NoPermissionException 예외를 던진다") {
                shouldThrow<NoPermissionException> {
                    stepService.updateStep(otherEnterpriseId, STEP_ID, updateStepRequest, guidelineFile)
                }
            }
        }
        When("deleteStep 실행 시") {
            Then("NoPermissionException 예외를 던진다") {
                shouldThrow<NoPermissionException> {
                    stepService.deleteStep(otherEnterpriseId, STEP_ID)
                }
            }
        }
    }

    Given("존재하지 않는 STEP에 대해서") {
        every { stepRepository.findByIdOrNull(any()) } returns null
        When("updateStep 실행 시") {
            Then("ModelNotFoundException 예외를 던진다") {
                shouldThrow<ModelNotFoundException> {
                    stepService.updateStep(ENTERPRISE_ID, STEP_ID, updateStepRequest, guidelineFile)
                }
            }
        }
        When("deleteStep 실행 시") {
            Then("ModelNotFoundException 예외를 던진다") {
                shouldThrow<ModelNotFoundException> {
                    stepService.deleteStep(ENTERPRISE_ID, STEP_ID)
                }
            }
        }
    }

    Given("getStepOverview 실행 시") {
        every { stepRepository.findByIdOrNull(any()) } returns step
        When("해당 step 이 속해있는 test 의 작성자가 아닐 경우") {
            Then("NoPermissionException 예외를 던진다") {
                shouldThrow<NoPermissionException> {
                    stepService.getStepOverview(2L, STEP_ID)
                }
            }
        }
    }


}) {
    companion object {
        private const val TEST_ID = 1L
        private const val ENTERPRISE_ID = 1L
        private const val STEP_ID = 1L
        private val mvpTest = MvpTest(
            id = TEST_ID,
            enterpriseId = ENTERPRISE_ID,
            mvpName = "string",
            recruitStartDate = LocalDateTime.of(2025, 5, 1, 12, 0),
            recruitEndDate = LocalDateTime.of(2025, 5, 5, 12, 0),
            testStartDate = LocalDateTime.of(2025, 5, 10, 12, 0),
            testEndDate = LocalDateTime.of(2025, 5, 15, 12, 0),
            mainImageUrl = "string",
            mvpInfo = "string",
            mvpUrl = "string",
            rewardBudget = 100000,
            requirementMinAge = 15,
            requirementMaxAge = 60,
            requirementSex = Sex.MALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 50,
            state = MvpTestState.APPROVED
        )

        private val createStepRequest = CreateStepRequest(
            title = "title",
            requirement = "requirement",
            reward = 100
        )
        private val updateStepRequest = UpdateStepRequest(
            title = "test_title",
            requirement = "test_requirement",
        )
        private const val MAX_ORDER = 0

        private val guidelineFile = MockMultipartFile(
            "file2",
            "test2.jpg",
            "image/jpeg",
            ByteArray(1)
        )
        private val step = Step(
            id = STEP_ID,
            title = createStepRequest.title,
            requirement = createStepRequest.requirement,
            guidelineUrl = "test.ppt",
            reward = createStepRequest.reward,
            stepOrder = MAX_ORDER + 1,
            mvpTest = mvpTest
        )
        private val invalidFile = MockMultipartFile(
            "invalidFile",
            "empty.jpg",
            "jpeg",
            ByteArray(1)
        )

        private val exceedMaxSizeFile = MockMultipartFile(
            "exceedMaxSizeFile",
            "exceedMaxSize.jpg",
            "jpg",
            ByteArray(15)
        )

        private val incorrectContentFile = MockMultipartFile(
            "incorrectContentFile",
            "incorrect.pdf",
            "jpg",
            ByteArray(1)
        )

    }
}






