package com.team1.mvp_test.domain.member.dto

import com.team1.mvp_test.domain.member.model.Member

data class MemberResponse(
    val id: Long,
    val name: String?,
    val email: String?,
    val age: Int?,
    val sex: String?,
    val info: String?,
    val point: Int,
    val state: String,
) {
    companion object {
        fun from(member: Member): MemberResponse {
            return MemberResponse(
                id = member.id!!,
                name = member.name,
                email = member.email,
                age = member.age,
                sex = member.sex?.name,
                info = member.info,
                point = member.point,
                state = member.state.name,
            )
        }
    }
}
