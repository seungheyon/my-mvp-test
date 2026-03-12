package com.team1.mvp_test.domain.member.dto

import com.team1.mvp_test.domain.member.model.Sex
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation

class MemberDtoTest : BehaviorSpec({
    val validator = Validation.buildDefaultValidatorFactory().validator

    Given("이름이 공백인 경우") {
        val request = SignUpInfoRequest(
            name = "",
            age = 15,
            sex = Sex.MALE,
            info = null,
            phoneNumber = "01012345678"
        )
        When("SignUpInfoRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "name"
            }
        }
    }

    Given("나이가 15 미만인 경우") {
        val request = SignUpInfoRequest(
            name = "이름",
            age = 10,
            sex = Sex.MALE,
            info = null,
            phoneNumber = "01012345678"
        )
        When("SignUpInfoRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "age"
            }
        }
    }

    Given("나이가 100 초과인 경우") {
        val request = SignUpInfoRequest(
            name = "이름",
            age = 101,
            sex = Sex.MALE,
            info = null,
            phoneNumber = "01012345678"
        )
        When("SignUpInfoRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "age"
            }
        }
    }

    Given("정보가 500자 초과인 경우") {
        val request = SignUpInfoRequest(
            name = "이름",
            age = 16,
            sex = Sex.MALE,
            info = "정보입니다".repeat(101),
            phoneNumber = "01012345678"
        )
        When("SignUpInfoRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "info"
            }
        }
    }

    Given("올바른 입력에 대해서") {
        val request = SignUpInfoRequest(
            name = "이름",
            age = 16,
            sex = Sex.MALE,
            info = "정보입니다",
            phoneNumber = "01012345678"
        )
        When("SignUpInfoRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과한다") {
                violations.size shouldBe 0
            }
        }
    }

})