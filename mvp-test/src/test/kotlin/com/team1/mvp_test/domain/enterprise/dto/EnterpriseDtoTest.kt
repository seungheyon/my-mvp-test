package com.team1.mvp_test.domain.enterprise.dto

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation

class EnterpriseDtoTest : BehaviorSpec({
    val validator = Validation.buildDefaultValidatorFactory().validator

    Given("이메일이 공백인 경우") {
        val request = EnterpriseSignUpRequest(
            email = "",
            password = "1234qwer",
            name = "팀1",
            ceoName = "김민수",
            phoneNumber = "01000000000"
        )
        When("EnterpriseSignupRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "email"
            }
        }
    }

    Given("이메일 형식이 아닌 경우") {
        val request = EnterpriseSignUpRequest(
            email = "email",
            password = "1234qwer",
            name = "팀1",
            ceoName = "김민수",
            phoneNumber = "01000000000"
        )
        When("EnterpriseSignupRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "email"
            }
        }
    }

    Given("비밀번호가 양식에 맞지 않는 경우") {
        val request = EnterpriseSignUpRequest(
            email = "email@email.com",
            password = "12345678",
            name = "팀1",
            ceoName = "김민수",
            phoneNumber = "01000000000"
        )
        When("EnterpriseSignupRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "password"
            }
        }
    }

    Given("비밀번호가 8자 미만인 경우") {
        val request = EnterpriseSignUpRequest(
            email = "email@email.com",
            password = "1234qwe",
            name = "팀1",
            ceoName = "김민수",
            phoneNumber = "01000000000"
        )
        When("EnterpriseSignupRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "password"
            }
        }
    }

    Given("기업명이 공백인 경우") {
        val request = EnterpriseSignUpRequest(
            email = "email@email.com",
            password = "1234qwer",
            name = "",
            ceoName = "김민수",
            phoneNumber = "01000000000"
        )
        When("EnterpriseSignupRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "name"
            }
        }
    }

    Given("대표자명이 공백인 경우") {
        val request = EnterpriseSignUpRequest(
            email = "email@email.com",
            password = "1234qwer",
            name = "팀1",
            ceoName = "",
            phoneNumber = "01000000000"
        )
        When("EnterpriseSignupRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "ceoName"
            }
        }
    }

    Given("전화번호 양식이 맞지 않는 경우") {
        val request = EnterpriseSignUpRequest(
            email = "email@email.com",
            password = "1234qwer",
            name = "팀1",
            ceoName = "김민수",
            phoneNumber = "12r123134"
        )
        When("EnterpriseSignupRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과하지 못한다") {
                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "phoneNumber"
            }
        }
    }

    Given("올바른 입력에 대해서") {
        val request = EnterpriseSignUpRequest(
            email = "email@email.com",
            password = "1234qwer",
            name = "팀1",
            ceoName = "김민수",
            phoneNumber = "01000000000"
        )
        When("EnterpriseSignupRequest 검증 시") {
            val violations = validator.validate(request)
            Then("통과한다") {
                violations.size shouldBe 0
            }
        }
    }
})