package com.team1.mvp_test.domain.mvptest.model

import com.team1.mvp_test.domain.member.model.Sex
import java.time.LocalDateTime

data class UpdateMvpTestObject(
    val mvpName: String,
    val recruitStartDate: LocalDateTime,
    val recruitEndDate: LocalDateTime,
    val testStartDate: LocalDateTime,
    val testEndDate: LocalDateTime,
    val mainImageUrl: String,
    val mvpInfo: String,
    val mvpUrl: String,
    val rewardBudget: Int,
    val requirementMinAge: Int?,
    val requirementMaxAge: Int?,
    val requirementSex: Sex,
    val recruitType: RecruitType,
    val recruitNum: Int,
)