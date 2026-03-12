package com.team1.mvp_test.domain.report.dto

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation

class ReportDtoTest : BehaviorSpec({
    val validator = Validation.buildDefaultValidatorFactory().validator

    Given("제목이 5자 미만인 경우") {
        val request = ReportRequest(
            title = "제목",
            body = "내용입니다",
            feedback = "피드백입니다"
        )
        When("ReportRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "title"
            }
        }
    }

    Given("제목이 50자 초과인 경우") {
        val request = ReportRequest(
            title = "제목".repeat(26),
            body = "내용입니다",
            feedback = "피드백입니다"
        )
        When("ReportRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "title"
            }
        }
    }

    Given("내용이 5자 미만인 경우") {
        val request = ReportRequest(
            title = "제목입니다",
            body = "내용",
            feedback = "피드백입니다"
        )
        When("ReportRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "body"
            }
        }
    }

    Given("내용이 500자 초과인 경우") {
        val request = ReportRequest(
            title = "제목입니다",
            body = "내용입니다".repeat(101),
            feedback = "피드백입니다"
        )
        When("ReportRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "body"
            }
        }
    }

    Given("피드백이 5자 미만인 경우") {
        val request = ReportRequest(
            title = "제목입니다",
            body = "내용입니다",
            feedback = "피드백"
        )
        When("ReportRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "feedback"
            }
        }
    }

    Given("피드백이 500자 초과인 경우") {
        val request = ReportRequest(
            title = "제목입니다",
            body = "내용입니다",
            feedback = "피드백입니다".repeat(100)
        )
        When("ReportRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "feedback"
            }
        }
    }

    Given("올바른 입력에 대해서") {
        val request = ReportRequest(
            title = "제목입니다",
            body = "내용입니다",
            feedback = "피드백입니다"
        )
        When("ReportRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과한다") {
                violations.size shouldBe 0
            }
        }
    }

})