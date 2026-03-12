package com.team1.mvp_test.domain.mvptest.dto

import com.team1.mvp_test.domain.member.model.Sex
import com.team1.mvp_test.domain.mvptest.model.RecruitType
import com.team1.mvp_test.domain.mvptest.model.UpdateMvpTestObject
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UpdateMvpTestRequest(
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

    val requirementSex: Sex,
    val recruitType: RecruitType,

    @field:Min(1)
    val recruitNum: Int,

    val categories: List<String>
) {
    fun toObject(mainImageUrl:String): UpdateMvpTestObject {
        return UpdateMvpTestObject(
            mvpName = this.mvpName,
            recruitStartDate = this.recruitStartDate,
            recruitEndDate = this.recruitEndDate,
            testStartDate = this.testStartDate,
            testEndDate = this.testEndDate,
            mainImageUrl = mainImageUrl,
            mvpInfo = this.mvpInfo,
            mvpUrl = this.mvpUrl,
            rewardBudget = this.rewardBudget,
            requirementMinAge = this.requirementMinAge,
            requirementMaxAge = this.requirementMaxAge,
            requirementSex = this.requirementSex,
            recruitType = this.recruitType,
            recruitNum = this.recruitNum,
        )

    }
}