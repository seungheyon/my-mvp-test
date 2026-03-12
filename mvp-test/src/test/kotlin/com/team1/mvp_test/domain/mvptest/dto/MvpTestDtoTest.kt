package com.team1.mvp_test.domain.mvptest.dto

import com.team1.mvp_test.domain.member.model.Sex
import com.team1.mvp_test.domain.mvptest.model.RecruitType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation
import java.time.LocalDateTime

class MvpTestDtoTest : BehaviorSpec({
    val validator = Validation.buildDefaultValidatorFactory().validator

    Given("공백이 포함된 경우") {
        val request = CreateMvpTestRequest(
            mvpName = "",
            recruitStartDate = LocalDateTime.of(2025, 7, 24, 0, 0),
            recruitEndDate = LocalDateTime.of(2025, 7, 26, 0, 0),
            testStartDate = LocalDateTime.of(2025, 8, 1, 0, 0),
            testEndDate = LocalDateTime.of(2025, 8, 2, 0, 0),
            mvpInfo = "",
            mvpUrl = "",
            rewardBudget = 10000,
            requirementMinAge = 15,
            requirementMaxAge = 20,
            requirementSex = Sex.FEMALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 10,
            categories = listOf()
        )
        When("CreateMvpRequest 검증 시") {
            val violations = validator.validate(request).toMutableList()
            Then("통과하지 못한다") {
                violations.size shouldBe 3
                violations.map { it.propertyPath.toString() }.sorted() shouldBe mutableListOf(
                    "mvpName", "mvpInfo", "mvpUrl"
                ).sorted()
            }
        }
    }

    Given("이름이 50자 이상인 경우") {
        val request = CreateMvpTestRequest(
            mvpName = "111111111111111".repeat(5),
            recruitStartDate = LocalDateTime.of(2025, 7, 24, 0, 0),
            recruitEndDate = LocalDateTime.of(2025, 7, 26, 0, 0),
            testStartDate = LocalDateTime.of(2025, 8, 1, 0, 0),
            testEndDate = LocalDateTime.of(2025, 8, 2, 0, 0),
            mvpInfo = "mvp 정보입니다",
            mvpUrl = "https://mvp.casd",
            rewardBudget = 10000,
            requirementMinAge = 15,
            requirementMaxAge = 20,
            requirementSex = Sex.FEMALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 10,
            categories = listOf()
        )
        When("CreateMvpRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "mvpName"
            }
        }
    }

    Given("리워드 총액이 10000미만인 경우") {
        val request = CreateMvpTestRequest(
            mvpName = "MVP 이름",
            recruitStartDate = LocalDateTime.of(2025, 7, 24, 0, 0),
            recruitEndDate = LocalDateTime.of(2025, 7, 26, 0, 0),
            testStartDate = LocalDateTime.of(2025, 8, 1, 0, 0),
            testEndDate = LocalDateTime.of(2025, 8, 2, 0, 0),
            mvpInfo = "mvp 정보입니다",
            mvpUrl = "https://mvp.casd",
            rewardBudget = 9999,
            requirementMinAge = 15,
            requirementMaxAge = 20,
            requirementSex = Sex.FEMALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 10,
            categories = listOf()
        )
        When("CreateMvpRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "rewardBudget"
            }
        }
    }

    Given("모집인원이 1미만인 경우") {
        val request = CreateMvpTestRequest(
            mvpName = "MVP 이름",
            recruitStartDate = LocalDateTime.of(2025, 7, 24, 0, 0),
            recruitEndDate = LocalDateTime.of(2025, 7, 26, 0, 0),
            testStartDate = LocalDateTime.of(2025, 8, 1, 0, 0),
            testEndDate = LocalDateTime.of(2025, 8, 2, 0, 0),
            mvpInfo = "mvp 정보입니다",
            mvpUrl = "https://mvp.casd",
            rewardBudget = 10000,
            requirementMinAge = 15,
            requirementMaxAge = 20,
            requirementSex = Sex.FEMALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = -1,
            categories = listOf()
        )
        When("CreateMvpRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "recruitNum"
            }
        }
    }

    Given("올바른 입력에 대해서") {
        val request = CreateMvpTestRequest(
            mvpName = "MVP 이름",
            recruitStartDate = LocalDateTime.of(2025, 7, 24, 0, 0),
            recruitEndDate = LocalDateTime.of(2025, 7, 26, 0, 0),
            testStartDate = LocalDateTime.of(2025, 8, 1, 0, 0),
            testEndDate = LocalDateTime.of(2025, 8, 2, 0, 0),
            mvpInfo = "mvp 정보입니다",
            mvpUrl = "https://mvp.casd",
            rewardBudget = 10000,
            requirementMinAge = 15,
            requirementMaxAge = 20,
            requirementSex = Sex.FEMALE,
            recruitType = RecruitType.FIRST_COME,
            recruitNum = 10,
            categories = listOf()
        )
        When("CreateMvpRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과한다") {
                violations.size shouldBe 0
            }
        }

    }

})