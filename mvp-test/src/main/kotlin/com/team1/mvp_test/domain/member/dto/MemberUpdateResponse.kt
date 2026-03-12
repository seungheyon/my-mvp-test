package com.team1.mvp_test.domain.member.dto

import com.team1.mvp_test.domain.member.model.Member

data class MemberUpdateResponse(
    val name: String?,
    val sex: String?,
    val info: String?,
) {
    companion object {
        fun from(member: Member): MemberUpdateResponse {
            return MemberUpdateResponse(
                name = member.name,
                sex = member.sex?.name,
                info = member.info,
            )
        }
    }
}