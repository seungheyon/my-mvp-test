package com.team1.mvp_test.domain.enterprise.service

import com.team1.mvp_test.common.Role
import com.team1.mvp_test.common.error.EnterpriseErrorMessage
import com.team1.mvp_test.common.exception.ModelNotFoundException
import com.team1.mvp_test.common.exception.NoPermissionException
import com.team1.mvp_test.common.exception.PasswordIncorrectException
import com.team1.mvp_test.domain.enterprise.dto.EnterpriseLoginResponse
import com.team1.mvp_test.domain.enterprise.dto.EnterpriseResponse
import com.team1.mvp_test.domain.enterprise.dto.EnterpriseSignUpRequest
import com.team1.mvp_test.domain.enterprise.dto.LoginRequest
import com.team1.mvp_test.domain.enterprise.model.Enterprise
import com.team1.mvp_test.domain.enterprise.model.EnterpriseState
import com.team1.mvp_test.domain.enterprise.repository.EnterpriseRepository
import com.team1.mvp_test.infra.security.jwt.JwtHelper
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class EnterpriseAuthService(
    private val enterpriseRepository: EnterpriseRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtHelper: JwtHelper,
) {

    fun signUp(request: EnterpriseSignUpRequest): EnterpriseResponse {
        check(!enterpriseRepository.existsByName(request.name)) { EnterpriseErrorMessage.ALREADY_EXISTS.message }
        check(!enterpriseRepository.existsByEmail(request.email)) { EnterpriseErrorMessage.ALREADY_EXISTS.message }
        return Enterprise(
            email = request.email,
            name = request.name,
            ceoName = request.ceoName,
            password = passwordEncoder.encode(request.password),
            phoneNumber = request.phoneNumber
        ).let { enterpriseRepository.save(it) }
            .let { EnterpriseResponse.from(it) }
    }

    fun login(request: LoginRequest): EnterpriseLoginResponse {
        val enterprise = enterpriseRepository.findByEmail(request.email)
            ?: throw ModelNotFoundException("Enterprise", request.email)
        if (!passwordEncoder.matches(request.password, enterprise.password)) throw PasswordIncorrectException()
        if (enterprise.state == EnterpriseState.REJECTED) throw NoPermissionException(EnterpriseErrorMessage.REJECTED_STATE.message)
        if (enterprise.state == EnterpriseState.PENDING) throw NoPermissionException(EnterpriseErrorMessage.PENDING_STATE.message)
        return jwtHelper.generateAccessToken(
            subject = enterprise.id!!.toString(),
            email = enterprise.email,
            role = Role.ENTERPRISE.name,
        ).let { EnterpriseLoginResponse(accessToken = it) }
    }
}