package com.team1.mvp_test.domain.enterprise.service

import com.team1.mvp_test.common.exception.ModelNotFoundException
import com.team1.mvp_test.domain.enterprise.dto.UpdateEnterpriseRequest
import com.team1.mvp_test.domain.enterprise.model.Enterprise
import com.team1.mvp_test.domain.enterprise.model.EnterpriseState
import com.team1.mvp_test.domain.enterprise.repository.EnterpriseRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull

class EnterpriseServiceTest : BehaviorSpec({
    val enterpriseRepository = mockk<EnterpriseRepository>(relaxed = true)
    val enterpriseService = EnterpriseService(enterpriseRepository)

    Given("getProfile 실행 시") {
        When("enterpriseId 가 존재하지 않으면") {
            every { enterpriseRepository.findByIdOrNull(any()) } returns null
            Then("ModelNotFoundException 예외 발생") {
                shouldThrowExactly<ModelNotFoundException> {
                    enterpriseService.getProfile(NONE_EXIST_ENTERPRISE_ID)
                }
            }
        }
    }

    Given("updateProfile 실행 시") {
        When("enterpriseId 가 존재하지 않을 경우") {
            every { enterpriseRepository.findByIdOrNull(any()) } returns null
            Then("ModelNotFoundException 예외 발생") {
                shouldThrowExactly<ModelNotFoundException> {
                    enterpriseService.updateProfile(NONE_EXIST_ENTERPRISE_ID, updateRequest)
                }
            }
        }
    }
}) {
    companion object {
        private const val NONE_EXIST_ENTERPRISE_ID = 1L
        private const val EXIST_ENTERPRISE_ID = 2L
        private val enterprise = Enterprise(
            id = EXIST_ENTERPRISE_ID,
            email = "test@test.com",
            name = "test enterprise name",
            ceoName = "test ceoName",
            password = "Test1234@",
            phoneNumber = "01012341234",
            state = EnterpriseState.APPROVED,
        )
        private val updateRequest = UpdateEnterpriseRequest(
            name = "new test enterprise name",
            ceoName = "new test ceoName",
            phoneNumber = "01098769876",
        )
    }
}