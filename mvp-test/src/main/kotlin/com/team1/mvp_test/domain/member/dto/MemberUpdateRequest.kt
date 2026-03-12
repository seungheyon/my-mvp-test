package com.team1.mvp_test.domain.member.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MemberUpdateRequest(
    @field:NotBlank
    val name: String,

    @field:Size(max = 500)
    val info: String?,
)