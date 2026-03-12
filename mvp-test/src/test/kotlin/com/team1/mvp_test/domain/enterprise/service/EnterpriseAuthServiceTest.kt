package com.team1.mvp_test.domain.enterprise.service

import com.team1.mvp_test.common.exception.ModelNotFoundException
import com.team1.mvp_test.common.exception.NoPermissionException
import com.team1.mvp_test.common.exception.PasswordIncorrectException
import com.team1.mvp_test.domain.enterprise.dto.EnterpriseSignUpRequest
import com.team1.mvp_test.domain.enterprise.dto.LoginRequest
import com.team1.mvp_test.domain.enterprise.model.Enterprise
import com.team1.mvp_test.domain.enterprise.model.EnterpriseState
import com.team1.mvp_test.domain.enterprise.repository.EnterpriseRepository
import com.team1.mvp_test.infra.security.jwt.JwtHelper
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.security.crypto.password.PasswordEncoder

class EnterpriseAuthServiceTest : BehaviorSpec({
    val enterpriseRepository = mockk<EnterpriseRepository>(relaxed = true)
    val passwordEncoder = mockk<PasswordEncoder>(relaxed = true)
    val jwtHelper = mockk<JwtHelper>(relaxed = true)

    val enterpriseAuthService = EnterpriseAuthService(
        enterpriseRepository = enterpriseRepository,
        passwordEncoder = passwordEncoder,
        jwtHelper = jwtHelper,
    )

    Given("signUp 실행 시") {
        When("이미 존재하는 이름인 경우") {
            every { enterpriseRepository.existsByName(any()) } returns true
            Then("Exception 발생") {
                shouldThrowExactly<IllegalStateException> {
                    enterpriseAuthService.signUp(signUpRequest)
                }
            }
        }
        When("이미 존재하는 이메일인 경우") {
            every { enterpriseRepository.existsByEmail(any()) } returns true
            Then("Exception 발생")
            shouldThrowExactly<IllegalStateException> {
                enterpriseAuthService.signUp(signUpRequest)
            }
        }
    }

    Given("login 실행 시") {
        When("존재하지 않는 이메일인 경우") {
            every { enterpriseRepository.findByEmail(any()) } returns null
            Then("ModelNotFoundException 발생") {
                shouldThrowExactly<ModelNotFoundException> {
                    enterpriseAuthService.login(loginRequest)
                }
            }
        }

        When("비밀번호가 일치하지 않은 경우") {
            every { enterpriseRepository.findByEmail(any()) } returns enterprise
            every { passwordEncoder.matches(any(), any()) } returns false
            Then("PasswordIncorrectException 발생") {
                shouldThrowExactly<PasswordIncorrectException> {
                    enterpriseAuthService.login(loginRequest)
                }
            }
        }

        When("state가 REJECTED인 경우") {
            val rejectedEnterprise = enterprise.apply { state = EnterpriseState.REJECTED }
            every { enterpriseRepository.findByEmail(any()) } returns rejectedEnterprise
            every { passwordEncoder.matches(any(), any()) } returns true
            Then("NoPermissionException 발생") {
                shouldThrowExactly<NoPermissionException> {
                    enterpriseAuthService.login(loginRequest)
                }
            }
        }

        When("state가 PENDING인 경우") {
            val pendingEnterprise = enterprise.apply { state = EnterpriseState.PENDING }
            every { enterpriseRepository.findByEmail(any()) } returns pendingEnterprise
            every { passwordEncoder.matches(any(), any()) } returns true
            Then("NoPermissionException 발생") {
                shouldThrowExactly<NoPermissionException> {
                    enterpriseAuthService.login(loginRequest)
                }
            }
        }
    }

}) {
    companion object {
        private const val ENTERPRISE_ID = 1L
        private const val EMAIL = "test@test.com"
        private const val PASSWORD = "Test1234@"
        private const val NAME = "test name"
        private const val CEO_NAME = "test ceo name"
        private const val PHONE_NUMBER = "01012345678"

        private val signUpRequest = EnterpriseSignUpRequest(
            email = EMAIL,
            name = NAME,
            ceoName = CEO_NAME,
            password = PASSWORD,
            phoneNumber = PHONE_NUMBER,
        )

        private val loginRequest = LoginRequest(
            email = EMAIL,
            password = PASSWORD,
        )

        private val enterprise = Enterprise(
            id = ENTERPRISE_ID,
            email = EMAIL,
            name = NAME,
            ceoName = CEO_NAME,
            password = PASSWORD,
            phoneNumber = PHONE_NUMBER,
            state = EnterpriseState.APPROVED,
        )
    }
}