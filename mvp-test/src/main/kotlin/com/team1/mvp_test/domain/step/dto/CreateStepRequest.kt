package com.team1.mvp_test.domain.step.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class CreateStepRequest(
    @field:Size(min = 5, max = 20)
    val title: String,

    @field:Size(min = 10, max = 200)
    val requirement: String,

    @field:Min(value = 1000)
    val reward: Int
)
