package com.team1.mvp_test.domain.mvptest.dto

import com.team1.mvp_test.domain.enterprise.model.Enterprise
import com.team1.mvp_test.domain.mvptest.model.MvpTest
import com.team1.mvp_test.domain.mvptest.model.RecruitType
import java.time.LocalDateTime

data class MvpTestResponse(
    val id: Long,
    val enterpriseId: Long,
    val enterpriseName: String,
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
    val requirementSex: String?,
    val recruitType: RecruitType,
    val recruitNum: Int,
    val categories: List<String>
) {
    companion object {
        fun from(mvpTest: MvpTest, enterprise: Enterprise, categories: List<String>): MvpTestResponse {
            return MvpTestResponse(
                id = mvpTest.id!!,
                enterpriseId = mvpTest.enterpriseId,
                enterpriseName = enterprise.name,
                mvpName = mvpTest.mvpName,
                recruitStartDate = mvpTest.recruitStartDate,
                recruitEndDate = mvpTest.recruitEndDate,
                testStartDate = mvpTest.testStartDate,
                testEndDate = mvpTest.testEndDate,
                mainImageUrl = mvpTest.mainImageUrl,
                mvpInfo = mvpTest.mvpInfo,
                mvpUrl = mvpTest.mvpUrl,
                rewardBudget = mvpTest.rewardBudget,
                requirementMinAge = mvpTest.requirementMinAge,
                requirementMaxAge = mvpTest.requirementMaxAge,
                requirementSex = mvpTest.requirementSex?.name,
                recruitType = mvpTest.recruitType,
                recruitNum = mvpTest.recruitNum,
                categories = categories
            )
        }
    }
}