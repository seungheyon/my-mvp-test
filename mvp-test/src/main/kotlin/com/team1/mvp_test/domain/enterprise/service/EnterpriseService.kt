package com.team1.mvp_test.domain.enterprise.service

import com.team1.mvp_test.common.exception.ModelNotFoundException
import com.team1.mvp_test.domain.enterprise.dto.EnterpriseResponse
import com.team1.mvp_test.domain.enterprise.dto.UpdateEnterpriseRequest
import com.team1.mvp_test.domain.enterprise.repository.EnterpriseRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EnterpriseService(
    private val enterpriseRepository: EnterpriseRepository
) {
    fun getProfile(enterpriseId: Long): EnterpriseResponse {
        val enterprise = enterpriseRepository.findByIdOrNull(enterpriseId)
            ?: throw ModelNotFoundException("Enterprise", enterpriseId)
        return EnterpriseResponse.from(enterprise)
    }

    @Transactional
    fun updateProfile(enterpriseId: Long, request: UpdateEnterpriseRequest): EnterpriseResponse {
        val enterprise = enterpriseRepository.findByIdOrNull(enterpriseId)
            ?: throw ModelNotFoundException("Enterprise", enterpriseId)
        enterprise.update(
            name = request.name,
            ceoName = request.ceoName,
            phoneNumber = request.phoneNumber,
        )
        return EnterpriseResponse.from(enterprise)
    }

    @Transactional
    fun getAllEnterprises(): List<EnterpriseResponse> {
        return enterpriseRepository.findAll().map {
            EnterpriseResponse.from(it)
        }
    }
}