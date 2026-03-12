package com.team1.mvp_test.domain.enterprise.controller

import com.team1.mvp_test.domain.enterprise.dto.*
import com.team1.mvp_test.domain.enterprise.service.EnterpriseAuthService
import com.team1.mvp_test.domain.enterprise.service.EnterpriseService
import com.team1.mvp_test.infra.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/enterprise")
class EnterpriseController(
    private val enterpriseService: EnterpriseService,
    private val enterpriseAuthService: EnterpriseAuthService
) {

    @PostMapping("/sign-up")
    fun signUp(
        @Valid @RequestBody request: EnterpriseSignUpRequest
    ): ResponseEntity<EnterpriseResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(enterpriseAuthService.signUp(request))
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest
    ): ResponseEntity<EnterpriseLoginResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(enterpriseAuthService.login(request))
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ENTERPRISE')")
    fun getProfile(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<EnterpriseResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(enterpriseService.getProfile(userPrincipal.id))
    }


    @PutMapping("/profile")
    @PreAuthorize("hasRole('ENTERPRISE')")
    fun updateProfile(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: UpdateEnterpriseRequest
    ): ResponseEntity<EnterpriseResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(enterpriseService.updateProfile(userPrincipal.id, request))
    }
}