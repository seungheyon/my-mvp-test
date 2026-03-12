package com.team1.mvp_test.admin.service

import com.team1.mvp_test.admin.dto.AdminLoginRequest
import com.team1.mvp_test.admin.dto.AdminLoginResponse
import com.team1.mvp_test.admin.repository.AdminRepository
import com.team1.mvp_test.common.Role
import com.team1.mvp_test.common.exception.ModelNotFoundException
import com.team1.mvp_test.common.exception.PasswordIncorrectException
import com.team1.mvp_test.infra.security.jwt.JwtHelper
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminAuthService(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtHelper: JwtHelper,
) {
    @Transactional
    fun login(request: AdminLoginRequest): AdminLoginResponse {
        val admin = adminRepository.findByAccount(request.account) ?: throw ModelNotFoundException("admin")
        if (!passwordEncoder.matches(request.password, admin.password)) throw PasswordIncorrectException()
        return jwtHelper.generateAccessToken(
            subject = admin.id!!.toString(),
            email = admin.account,
            role = Role.ADMIN.name,
        ).let { AdminLoginResponse(accessToken = it) }
    }
}