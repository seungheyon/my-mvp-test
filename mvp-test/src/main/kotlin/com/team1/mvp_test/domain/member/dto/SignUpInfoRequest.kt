package com.team1.mvp_test.domain.member.dto

import com.team1.mvp_test.domain.member.model.Sex
import jakarta.validation.constraints.*

data class SignUpInfoRequest(
    @field:NotBlank
    val name: String,

    @field:Min(value = 15)
    @field:Max(value = 100)
    val age: Int,

    val sex: Sex,

    @field:Pattern(regexp = "^01([0-1])([0-9]{3,4})([0-9]){4}$")
    val phoneNumber: String,

    @field:Size(max = 500)
    val info: String?,
)
