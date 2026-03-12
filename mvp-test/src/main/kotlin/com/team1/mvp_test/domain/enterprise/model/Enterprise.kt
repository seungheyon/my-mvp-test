package com.team1.mvp_test.domain.enterprise.model

import jakarta.persistence.*

@Entity
@Table(name = "enterprise")
class Enterprise(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "email")
    val email: String,

    @Column(name = "name")
    var name: String,

    @Column(name = "ceo_name")
    var ceoName: String,

    @Column(name = "password")
    var password: String,

    @Column(name = "phone_number")
    var phoneNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    var state: EnterpriseState = EnterpriseState.PENDING,

    @Column(name = "reason")
    var reason: String? = null,
) {
    fun update(name: String, ceoName: String, phoneNumber: String) {
        this.name = name
        this.ceoName = ceoName
        this.phoneNumber = phoneNumber
    }

    fun approve() {
        this.state = EnterpriseState.APPROVED
    }

    fun reject(reason: String) {
        this.state = EnterpriseState.REJECTED
        this.reason = reason
    }

    fun block(reason: String) {
        this.state = EnterpriseState.BLOCKED
        this.reason = reason
    }
}