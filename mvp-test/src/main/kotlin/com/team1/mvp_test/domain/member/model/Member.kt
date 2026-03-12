package com.team1.mvp_test.domain.member.model

import com.team1.mvp_test.domain.oauth.provider.OAuthProvider
import jakarta.persistence.*

@Entity
@Table(name = "members")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name")
    var name: String? = null,

    @Column(name = "email")
    val email: String,

    @Column(name = "age")
    var age: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "sex")
    var sex: Sex? = null,

    @Column(name = "info")
    var info: String? = null,

    @Column(name = "phone_number")
    var phoneNumber: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_name")
    val providerName: OAuthProvider? = null,

    @Column(name = "provider_id")
    val providerId: String? = null,

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    var state: MemberState = MemberState.PENDING,

    @Column(name = "point")
    var point: Int = 0,

    @Column(name = "reason")
    var reason: String? = null,
) {
    fun updateMember(name: String, info: String?) {
        this.name = name
        this.info = info
    }

    fun updateSignUpInfo(name: String, age: Int, sex: Sex, info: String?, phoneNumber: String) {
        this.name = name
        this.age = age
        this.sex = sex
        this.info = info
        this.phoneNumber = phoneNumber
        this.state = MemberState.ACTIVE
    }

    fun settleReward(reward: Int) {
        point += reward
    }

    fun active() {
        this.state = MemberState.ACTIVE
    }

    fun block(reason: String) {
        this.state = MemberState.BLOCKED
        this.reason = reason
    }
}