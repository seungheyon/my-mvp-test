package com.team1.mvp_test.domain.step.dto

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation

class StepDtoTest : BehaviorSpec({
    val validator = Validation.buildDefaultValidatorFactory().validator

    Given("제목이 5자 미만인 경우") {
        val request = CreateStepRequest(
            title = "제목",
            requirement = "회원가입 후 사진을 촬영해서 업로드해주세요",
            reward = 10000
        )
        When("CreateStepRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "title"
            }
        }
    }

    Given("제목이 20자 초과인 경우") {
        val request = CreateStepRequest(
            title = "제목".repeat(11),
            requirement = "회원가입 후 사진을 촬영해서 업로드해주세요",
            reward = 10000
        )
        When("CreateStepRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "title"
            }
        }
    }

    Given("요구사항이 10자 미만인 경우") {
        val request = CreateStepRequest(
            title = "제목입니다",
            requirement = "요구사항",
            reward = 10000
        )
        When("CreateStepRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "requirement"
            }
        }
    }

    Given("요구사항이 200자 초과인 경우") {
        val request = CreateStepRequest(
            title = "제목입니다",
            requirement = "요구사항".repeat(100),
            reward = 10000
        )
        When("CreateStepRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "requirement"
            }
        }
    }

    Given("리워드가 1000 미만인 경우") {
        val request = CreateStepRequest(
            title = "제목입니다",
            requirement = "회원가입 후 사진을 촬영해서 업로드해주세요",
            reward = 999
        )
        When("CreateStepRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "reward"
            }
        }
    }

    Given("올바른 입력에 대해서") {
        val request = CreateStepRequest(
            title = "제목입니다",
            requirement = "회원가입 후 사진을 촬영해서 업로드해주세요",
            reward = 1000
        )
        When("CreateStepRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과한다") {
                violations.size shouldBe 0
            }
        }
    }

})