package com.team1.mvp_test.admin.dto.adminauthority

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class RejectRequest(
    @JsonProperty("reason")
    @field:NotBlank
    val reason: String
)
