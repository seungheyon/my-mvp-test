package com.team1.mvp_test.domain.mvptest.model

import com.team1.mvp_test.domain.member.model.Sex
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Table(name = "mvp_test")
@Entity
class MvpTest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "enterprise_id")
    val enterpriseId: Long,

    @Column(name = "mvp_name")
    var mvpName: String,

    @Column(name = "recruit_start_date")
    var recruitStartDate: LocalDateTime,

    @Column(name = "recruit_ent_date")
    var recruitEndDate: LocalDateTime,

    @Column(name = "test_start_date")
    var testStartDate: LocalDateTime,

    @Column(name = "test_end_date")
    var testEndDate: LocalDateTime,

    @Column(name = "main_image_url")
    var mainImageUrl: String,

    @Column(name = "mvp_info")
    var mvpInfo: String,

    @Column(name = "mvp_url")
    var mvpUrl: String,

    @Column(name = "reword_budget")
    var rewardBudget: Int,

    @Column(name = "requirement_min_age")
    var requirementMinAge: Int? = null,

    @Column(name = "requirement_max_age")
    var requirementMaxAge: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_sex")
    var requirementSex: Sex? = null,

    @Column(name = "recruit_type")
    @Enumerated(EnumType.STRING)
    var recruitType: RecruitType,

    @Column(name = "recruit_num")
    var recruitNum: Int,

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    var state: MvpTestState,

    @Column(name = "reason")
    var reason: String? = null,

    @Column(name = "settlement_date")
    var settlementDate: LocalDate? = null
) {

    fun update(updateObject: UpdateMvpTestObject) {
        this.mvpName = updateObject.mvpName
        this.recruitStartDate = updateObject.recruitStartDate
        this.recruitEndDate = updateObject.recruitEndDate
        this.testStartDate = updateObject.testStartDate
        this.testEndDate = updateObject.testEndDate
        this.mainImageUrl = updateObject.mainImageUrl
        this.mvpInfo = updateObject.mvpInfo
        this.mvpUrl = updateObject.mvpUrl
        this.rewardBudget = updateObject.rewardBudget
        this.requirementMinAge = updateObject.requirementMinAge
        this.requirementMaxAge = updateObject.requirementMaxAge
        this.requirementSex = updateObject.requirementSex
        this.recruitType = updateObject.recruitType
        this.recruitNum = updateObject.recruitNum
    }

    fun approve() {
        this.state = MvpTestState.APPROVED
    }

    fun reject(reason: String) {
        this.state = MvpTestState.REJECTED
        this.reason = reason
    }
}