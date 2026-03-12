package com.team1.mvp_test.domain.mvptest.dto

import com.team1.mvp_test.domain.member.model.Sex
import com.team1.mvp_test.domain.mvptest.model.MvpTest
import com.team1.mvp_test.domain.mvptest.model.MvpTestState
import com.team1.mvp_test.domain.mvptest.model.RecruitType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CreateMvpTestRequest(
    @field:NotBlank
    @field:Size(max = 50)
    val mvpName: String,

    val recruitStartDate: LocalDateTime,
    val recruitEndDate: LocalDateTime,
    val testStartDate: LocalDateTime,
    val testEndDate: LocalDateTime,

    @field:NotBlank
    val mvpInfo: String,

    @field:NotBlank
    val mvpUrl: String,

    @field:Min(10000)
    val rewardBudget: Int,

    val requirementMinAge: Int?,
    val requirementMaxAge: Int?,
    val requirementSex: Sex?,
    val recruitType: RecruitType,

    @field:Min(1)
    val recruitNum: Int,

    val categories: List<String>
) {
    fun toMvpTest(enterpriseId: Long, mainImageUrl: String): MvpTest {
        return MvpTest(
            enterpriseId = enterpriseId,
            mvpName = mvpName,
            recruitStartDate = recruitStartDate,
            recruitEndDate = recruitEndDate,
            testStartDate = testStartDate,
            testEndDate = testEndDate,
            mainImageUrl = mainImageUrl,
            mvpInfo = mvpInfo,
            mvpUrl = mvpUrl,
            rewardBudget = rewardBudget,
            requirementMinAge = requirementMinAge,
            requirementMaxAge = requirementMaxAge,
            requirementSex = requirementSex,
            recruitType = recruitType,
            recruitNum = recruitNum,
            state = MvpTestState.PENDING
        )
    }
}