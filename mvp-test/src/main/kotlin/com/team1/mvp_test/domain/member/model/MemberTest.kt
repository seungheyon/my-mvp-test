package com.team1.mvp_test.domain.member.model

import com.team1.mvp_test.domain.mvptest.model.MvpTest
import jakarta.persistence.*

@Entity
@Table(name = "member_test")
class MemberTest(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "member_id")
    val member: Member,

    @ManyToOne
    @JoinColumn(name = "test_id")
    val test: MvpTest,

    @Column(name = "state")
    val state: MemberTestState = MemberTestState.PENDING
) {
}