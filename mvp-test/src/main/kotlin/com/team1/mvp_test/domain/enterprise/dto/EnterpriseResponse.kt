package com.team1.mvp_test.domain.enterprise.dto

import com.team1.mvp_test.domain.enterprise.model.Enterprise

data class EnterpriseResponse(
    val id: Long,
    val email: String,
    val name: String,
    val ceoName: String,
    val phoneNumber: String,
    val state: String,
) {
    companion object {
        fun from(enterprise: Enterprise): EnterpriseResponse {
            return EnterpriseResponse(
                id = enterprise.id!!,
                email = enterprise.email,
                name = enterprise.name,
                ceoName = enterprise.ceoName,
                phoneNumber = enterprise.phoneNumber,
                state = enterprise.state.name
            )
        }
    }
}