package com.team1.mvp_test.domain.member.model

import jakarta.persistence.*

@Entity
@Table(name = "member_reward")
class MemberReward(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "member_id")
    val memberId: Long,

    @Column(name = "amount")
    val amount: Int,

    @Column(name = "message")
    val message: String,
)



