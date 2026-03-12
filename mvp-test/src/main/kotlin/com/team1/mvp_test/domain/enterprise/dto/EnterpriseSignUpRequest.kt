package com.team1.mvp_test.domain.enterprise.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class EnterpriseSignUpRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$")
    val password: String,

    @field:NotBlank
    val name: String,

    @field:NotBlank
    val ceoName: String,

    @field:Pattern(regexp = "^01([0-1])([0-9]{3,4})([0-9]){4}$")
    val phoneNumber: String,
)