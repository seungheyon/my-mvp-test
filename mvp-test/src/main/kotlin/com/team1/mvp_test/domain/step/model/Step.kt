package com.team1.mvp_test.domain.step.model

import com.team1.mvp_test.domain.mvptest.model.MvpTest
import jakarta.persistence.*

@Table(name = "step")
@Entity
class Step(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "title")
    var title: String,

    @Column(name = "requirement")
    var requirement: String,

    @Column(name = "guideline_url")
    var guidelineUrl: String? = null,

    @Column(name = "step_order")
    var stepOrder: Int,

    @Column(name = "reward")
    var reward: Int,

    @ManyToOne
    @JoinColumn(name = "mvp_test_id")
    var mvpTest: MvpTest,
) {
    fun updateStep(title: String, requirement: String, guidelineUrl: String?) {
        this.title = title
        this.requirement = requirement
        this.guidelineUrl = guidelineUrl
    }
}