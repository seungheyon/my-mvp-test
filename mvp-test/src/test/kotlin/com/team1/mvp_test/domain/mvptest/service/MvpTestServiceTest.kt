package com.team1.mvp_test.domain.mvptest.service

import com.team1.mvp_test.common.exception.ModelNotFoundException
import com.team1.mvp_test.common.exception.NoPermissionException
import com.team1.mvp_test.domain.category.repository.CategoryRepository
import com.team1.mvp_test.domain.enterprise.model.Enterprise
import com.team1.mvp_test.domain.enterprise.model.EnterpriseState
import com.team1.mvp_test.domain.enterprise.repository.EnterpriseRepository
import com.team1.mvp_test.domain.member.model.Member
import com.team1.mvp_test.domain.member.model.MemberState
import com.team1.mvp_test.domain.member.model.Sex
import com.team1.mvp_test.domain.member.repository.MemberRepository
import com.team1.mvp_test.domain.member.repository.MemberTestRepository
import com.team1.mvp_test.domain.member.service.MemberService
import com.team1.mvp_test.domain.mvptest.dto.CreateMvpTestRequest
import com.team1.mvp_test.domain.mvptest.dto.UpdateMvpTestRequest
import com.team1.mvp_test.domain.mvptest.model.MvpTest
import com.team1.mvp_test.domain.mvptest.model.MvpTestState
import com.team1.mvp_test.domain.mvptest.model.RecruitType
import com.team1.mvp_test.domain.mvptest.repository.MvpTestCategoryMapRepository
import com.team1.mvp_test.domain.mvptest.repository.MvpTestRepository
import com.team1.mvp_test.infra.redisson.RedissonService
import com.team1.mvp_test.infra.s3.s3service.S3ServiceImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDateTime

class MvpTestServiceTest : BehaviorSpec({
    isolationMode = IsolationMode.SingleInstance

    val mvpTestRepository = mockk<MvpTestRepository>()
    val categoryRepository = mockk<CategoryRepository>()
    val mvpTestCategoryMapRepository = mockk<MvpTestCategoryMapRepository>()
    val s3Service = mockk<S3ServiceImpl>()
    val memberRepository = mockk<MemberRepository>()
    val memberTestRepository = mockk<MemberTestRepository>()
    val memberService = mockk<MemberService>(relaxed = true)
    val redissonService = mockk<RedissonService>()
    val enterpriseRepository = mockk<EnterpriseRepository>()

    val mvpTestService = MvpTestService(
        mvpTestRepository = mvpTestRepository,
        categoryRepository = categoryRepository,
        mvpTestCategoryMapRepository = mvpTestCategoryMapRepository,
        memberRepository = memberRepository,
        memberTestRepository = memberTestRepository,
        s3Service = s3Service,
        redissonService = redissonService,
        memberService = memberService,
        enterpriseRepository = enterpriseRepository,
    )

    Given("파일 형식이 jpg, jpeg, png 이 아니면 ") {
        every { enterpriseRepository.findByIdOrNull(any()) } returns enterprise
        every { s3Service.uploadMvpTestFile(any()) } throws IllegalArgumentException()
        When("createMvpTest 실행") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    mvpTestService.createMvpTest(ENTERPRISE_ID, createMvpTestRequest, invalidFile)
                }
            }
        }
    }

    Given("파일 확장자가 jpg,png이면서 파일 내용이 png,jpg가 아니라면") {
        every { enterpriseRepository.findByIdOrNull(any()) } returns enterprise
        every { s3Service.uploadMvpTestFile(any()) } throws IllegalArgumentException()
        When("createMvpTest 실행") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    mvpTestService.createMvpTest(ENTERPRISE_ID, createMvpTestRequest, incorrectContentFile)
                }
            }
        }
    }


    Given("파일이 없으면 ") {
        every { enterpriseRepository.findByIdOrNull(any()) } returns enterprise
        every { s3Service.uploadMvpTestFile(any()) } throws IllegalArgumentException()
        When("createMvpTest 실행 ") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    mvpTestService.createMvpTest(ENTERPRISE_ID, createMvpTestRequest, emptyFile)
                }
            }
        }
    }

    Given("업로드 파일 용량이 10MB 초과일때") {
        every { enterpriseRepository.findByIdOrNull(any()) } returns enterprise
        every { s3Service.uploadMvpTestFile(any()) } throws IllegalArgumentException()
        When("createMvpTest 실행 시") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    mvpTestService.createMvpTest(ENTERPRISE_ID, createMvpTestRequest, exceedMaxSizeFile)
                }
            }
        }
    }

    Given("파일 형식이 jpg, jpeg, png 이 아니면") {
        every { s3Service.uploadMvpTestFile(any()) } throws IllegalArgumentException()
        every { mvpTestRepository.findByIdOrNull(any()) } returns mvpTest
        every { s3Service.deleteFile(any()) } returns Unit
        every { mvpTestCategoryMapRepository.findAllByMvpTestId(any()) } returns emptyList()
        every { mvpTestCategoryMapRepository.deleteAll(any()) } returns Unit
        When("updateMvpTest 실행") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    mvpTestService.updateMvpTest(ENTERPRISE_ID, TEST_ID, updateMvpTestRequest, invalidFile)
                }
            }
        }
    }

    Given("파일 확장자가 jpg,png이면서 파일 내용이 png,jpg가 아니면") {
        every { s3Service.uploadMvpTestFile(any()) } throws IllegalArgumentException()
        every { mvpTestRepository.findByIdOrNull(any()) } returns mvpTest
        every { s3Service.deleteFile(any()) } returns Unit
        every { mvpTestCategoryMapRepository.findAllByMvpTestId(any()) } returns emptyList()
        every { mvpTestCategoryMapRepository.deleteAll(any()) } returns Unit
        When("updateMvpTest 실행") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    mvpTestService.updateMvpTest(ENTERPRISE_ID, TEST_ID, updateMvpTestRequest, incorrectContentFile)
                }
            }
        }
    }



    Given("파일이 없으면") {
        every { s3Service.uploadMvpTestFile(any()) } throws IllegalArgumentException("Invalid file type. Only JPEG and PNG are allowed.")
        every { mvpTestRepository.findByIdOrNull(any()) } returns mvpTest
        every { s3Service.deleteFile(any()) } returns Unit
        every { mvpTestCategoryMapRepository.findAllByMvpTestId(any()) } returns emptyList()
        every { mvpTestCategoryMapRepository.deleteAll(any()) } returns Unit
        When("updateMvpTest 실행") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    mvpTestService.updateMvpTest(ENTERPRISE_ID, TEST_ID, updateMvpTestRequest, emptyFile)
                }
            }
        }
    }

    Given("업로드 파일 용량이 10MB 초과라면") {
        every { mvpTestRepository.findByIdOrNull(any()) } returns mvpTest
        every { s3Service.deleteFile(any()) } returns Unit
        every { mvpTestCategoryMapRepository.findAllByMvpTestId(any()) } returns emptyList()
        every { mvpTestCategoryMapRepository.deleteAll(any()) } returns Unit
        every { s3Service.uploadMvpTestFile(any()) } throws IllegalArgumentException()
        When("createMvpTest 실행 시") {
            Then("IllegalArgumentException 예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    mvpTestService.updateMvpTest(ENTERPRISE_ID, TEST_ID, updateMvpTestRequest, exceedMaxSizeFile)
                }
            }
        }
    }

    Given("member 의 state 가 active 가 아닐때") {
        every { mvpTestRepository.findByIdOrNull(TEST_ID) } returns mvpTest
        val member = Member(id = 1L, email = "test@test.com", state = MemberState.PENDING)
        every { memberRepository.findByIdOrNull(any()) } returns member
        When("applyMvpTest 실행시") {
            Then("IllegalStateException 발생")
            shouldThrow<IllegalStateException> {
                mvpTestService.applyToMvpTest(1L, TEST_ID)
            }
        }
    }

    Given("member 의 sex 가 test 의 requireSex 가 아닐경우") {
        every { mvpTestRepository.findByIdOrNull(TEST_ID) } returns mvpTest
        val member = Member(id = 1L, email = "test@test.com", state = MemberState.ACTIVE, sex = Sex.FEMALE)
        every { memberRepository.findByIdOrNull(any()) } returns member
        When("applyMvpTest 실행시") {
            Then("IllegalStateException 발생") {
                shouldThrow<IllegalStateException> {
                    mvpTestService.applyToMvpTest(1L, TEST_ID)
                }
            }
        }
    }
    Given("member 의 age 가 test 의 requireMinAge 보다 작으면") {
        every { mvpTestRepository.findByIdOrNull(TEST_ID) } returns mvpTest
        val member = Member(id = 1L, email = "test@test.com", state = MemberState.ACTIVE, sex = Sex.FEMALE, age = 10)
        every { memberRepository.findByIdOrNull(any()) } returns member
        When("applyMvpTest 실행시") {
            Then("IllegalStateException 발생") {
                shouldThrow<IllegalStateException> {
                    mvpTestService.applyToMvpTest(1L, TEST_ID)
                }
            }
        }
    }

    Given("member 의 age 가 test 의 requireMaxAge 보다 크면") {
        every { mvpTestRepository.findByIdOrNull(TEST_ID) } returns mvpTest
        val member = Member(id = 1L, email = "test@test.com", state = MemberState.ACTIVE, sex = Sex.FEMALE, age = 100)
        every { memberRepository.findByIdOrNull(any()) } returns member
        When("applyMvpTest 실행시") {
            Then("IllegalStateException 발생") {
                shouldThrow<IllegalStateException> {
                    mvpTestService.applyToMvpTest(1L, TEST_ID)
                }
            }
        }
    }

    Given("기업이 존재하지 않는 경우") {
        every { enterpriseRepository.findByIdOrNull(any()) } returns null
        When("createMvpTest 실행 시") {
            Then("ModelNotFoundException 발생") {
                shouldThrow<ModelNotFoundException> {
                    mvpTestService.createMvpTest(1L, createMvpTestRequest, invalidFile)
                }
            }
        }
    }
    Given("기업이 BLOCKED 상태인 경우") {
        every { enterpriseRepository.findByIdOrNull(any()) } returns blockedEnterprise
        When("createMvpTest 실행 시") {
            Then("NoPermissionException 발생") {
                shouldThrow<NoPermissionException> {
                    mvpTestService.createMvpTest(2L, createMvpTestRequest, invalidFile)
                }
            }
        }
    }
    Given("날짜 조건을 만족하지 않는 경우") {
        every { enterpriseRepository.findByIdOrNull(any()) } returns enterprise
        When("createMvpTest 실행 시") {
            Then("IllegalArgumentException 발생") {
                shouldThrow<IllegalArgumentException> {
                    mvpTestService.createMvpTest(1L, invalidCreateMvpTestRequest1, invalidFile)
                    mvpTestService.createMvpTest(1L, invalidCreateMvpTestRequest2, invalidFile)
                    mvpTestService.createMvpTest(1L, invalidCreateMvpTestRequest3, invalidFile)
                    mvpTestService.createMvpTest(1L, invalidCreateMvpTestRequest4, invalidFile)
                }
            }
        }
    }

    Given("테스트가 존재하지 않는 경우") {
        every { mvpTestRepository.findByIdOrNull(any()) } returns null
        When("deleteMvpTest 실행 시") {
            Then("ModelNotFoundException 발생") {
                shouldThrow<ModelNotFoundException> {
                    mvpTestService.deleteMvpTest(1L, 1L)
                }
            }
        }
        When("getMvpTest 실행 시") {
            Then("ModelNotFoundException 발생") {
                shouldThrow<ModelNotFoundException> {
                    mvpTestService.getMvpTest(1L)
                }
            }
        }
    }

    Given("권한이 없는 기업인 경우") {
        every { mvpTestRepository.findByIdOrNull(any()) } returns mvpTest
        When("deleteMvpTest 실행 시") {
            Then("NoPermissionException 발생") {
                shouldThrow<NoPermissionException> {
                    mvpTestService.deleteMvpTest(BLOCKED_ENTERPRISE_ID, 1L)
                }
            }
        }
    }


}) {
    companion object {
        private const val TEST_ID = 1L
        private const val ENTERPRISE_ID = 1L
        private const val BLOCKED_ENTERPRISE_ID = 2L
        private val mvpTest = MvpTest(
            id = TEST_ID,
            enterpriseId = ENTERPRISE_ID,
            mvpName = "string",
            recruitStartDate = LocalDateTime.of(2025, 5, 1, 12, 0),
            recruitEndDate = LocalDateTime.of(2025, 5, 5, 12, 0),
            testStartDate = LocalDateTime.of(2025, 5, 10, 12, 0),
            testEndDate = LocalDateTime.of(2025, 5, 15, 12, 0),
            mainImageUrl = "string.jpg",
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
        private val enterprise = Enterprise(
            id = ENTERPRISE_ID,
            email = "test@test.com",
            name = "testName",
            ceoName = "testCeoName",
            password = "pWdjH0wrRuybq9ccRSDug2Z",
            phoneNumber = "01012345678",
            state = EnterpriseState.APPROVED,
            reason = null
        )

        private val blockedEnterprise = Enterprise(
            id = BLOCKED_ENTERPRISE_ID,
            email = "test@test.com",
            name = "testName",
            ceoName = "testCeoName",
            password = "pWdjH0wrRuybq9ccRSDug2Z",
            phoneNumber = "01012345678",
            state = EnterpriseState.BLOCKED,
            reason = null
        )


        private val createMvpTestRequest = CreateMvpTestRequest(
            mvpName = "Valid MVP",
            recruitStartDate = LocalDateTime.of(2025, 7, 24, 0, 0),
            recruitEndDate = LocalDateTime.of(2025, 7, 26, 0, 0),
            testStartDate = LocalDateTime.of(2025, 8, 1, 0, 0),
            testEndDate = LocalDateTime.of(2025, 8, 2, 0, 0),
            mvpInfo = "Valid info",
            mvpUrl = "https://mvp.casd",
            rewardBudget = 10000,
            requirementMinAge = 15,
            requirementMaxAge = 20,
            requirementSex = Sex.FEMALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 10,
            categories = listOf("ValidCategory")
        )
        private val invalidCreateMvpTestRequest1 = CreateMvpTestRequest(
            mvpName = "Valid MVP",
            recruitStartDate = LocalDateTime.of(2025, 7, 26, 0, 0),
            recruitEndDate = LocalDateTime.of(2025, 7, 24, 0, 0),
            testStartDate = LocalDateTime.of(2025, 8, 1, 0, 0),
            testEndDate = LocalDateTime.of(2025, 8, 2, 0, 0),
            mvpInfo = "Valid info",
            mvpUrl = "https://mvp.casd",
            rewardBudget = 10000,
            requirementMinAge = 15,
            requirementMaxAge = 20,
            requirementSex = Sex.FEMALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 10,
            categories = listOf("ValidCategory")
        )
        private val invalidCreateMvpTestRequest2 = CreateMvpTestRequest(
            mvpName = "Valid MVP",
            recruitStartDate = LocalDateTime.of(2025, 7, 24, 0, 0),
            recruitEndDate = LocalDateTime.of(2025, 7, 26, 0, 0),
            testStartDate = LocalDateTime.of(2025, 7, 25, 0, 0),
            testEndDate = LocalDateTime.of(2025, 7, 27, 0, 0),
            mvpInfo = "Valid info",
            mvpUrl = "https://mvp.casd",
            rewardBudget = 10000,
            requirementMinAge = 15,
            requirementMaxAge = 20,
            requirementSex = Sex.FEMALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 10,
            categories = listOf("ValidCategory")
        )
        private val invalidCreateMvpTestRequest3 = CreateMvpTestRequest(
            mvpName = "Valid MVP",
            recruitStartDate = LocalDateTime.of(2025, 7, 24, 0, 0),
            recruitEndDate = LocalDateTime.of(2025, 7, 26, 0, 0),
            testStartDate = LocalDateTime.of(2025, 7, 27, 0, 0),
            testEndDate = LocalDateTime.of(2025, 7, 26, 0, 0),
            mvpInfo = "Valid info",
            mvpUrl = "https://mvp.casd",
            rewardBudget = 10000,
            requirementMinAge = 15,
            requirementMaxAge = 20,
            requirementSex = Sex.FEMALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 10,
            categories = listOf("ValidCategory")
        )
        private val invalidCreateMvpTestRequest4 = CreateMvpTestRequest(
            mvpName = "Valid MVP",
            recruitStartDate = LocalDateTime.of(2025, 7, 24, 0, 0),
            recruitEndDate = LocalDateTime.of(2025, 7, 26, 0, 0),
            testStartDate = LocalDateTime.of(2025, 7, 27, 0, 0),
            testEndDate = LocalDateTime.of(2025, 7, 28, 0, 0),
            mvpInfo = "Valid info",
            mvpUrl = "https://mvp.casd",
            rewardBudget = 10000,
            requirementMinAge = 20,
            requirementMaxAge = 15,
            requirementSex = Sex.FEMALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 10,
            categories = listOf("ValidCategory")
        )
        private val updateMvpTestRequest = UpdateMvpTestRequest(
            mvpName = "Valid MVP Name",
            recruitStartDate = LocalDateTime.of(2025, 9, 1, 12, 0),
            recruitEndDate = LocalDateTime.of(2025, 9, 5, 12, 0),
            testStartDate = LocalDateTime.of(2025, 9, 10, 12, 0),
            testEndDate = LocalDateTime.of(2025, 9, 15, 12, 0),
            mvpInfo = "Valid info",
            mvpUrl = "https://mvp.casd",
            rewardBudget = 10000,
            requirementMinAge = 15,
            requirementMaxAge = 20,
            requirementSex = Sex.FEMALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 10,
            categories = listOf()
        )

        private val emptyFile = MockMultipartFile(
            "emptyFile",
            "empty.jpg",
            "jpg",
            ByteArray(0)
        )

        private val invalidFile = MockMultipartFile(
            "invalidFile",
            "test.pdf",
            "pdf",
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
            "incorrect.jpg",
            "pdf",
            ByteArray(1)
        )
    }

}
