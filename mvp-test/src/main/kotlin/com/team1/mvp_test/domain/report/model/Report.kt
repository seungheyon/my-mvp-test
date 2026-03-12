package com.team1.mvp_test.domain.report.model

import com.team1.mvp_test.domain.member.model.MemberTest
import com.team1.mvp_test.domain.step.model.Step
import jakarta.persistence.*

@Entity
@Table(name = "report")
class Report(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "title")
    var title: String,

    @Column(name = "body")
    var body: String,

    @Column(name = "feedback")
    var feedback: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    var state: ReportState = ReportState.PENDING,

    @Column(name = "reason")
    var reason: String? = null,

    @ManyToOne
    @JoinColumn(name = "step_id")
    val step: Step,

    @ManyToOne
    @JoinColumn(name = "member_test_id")
    val memberTest: MemberTest,

    @OneToMany
    @JoinColumn(name = "report_id")
    var reportMedia: MutableList<ReportMedia> = mutableListOf(),

    ) {
    fun addReportMedia(media: ReportMedia) {
        reportMedia.add(media)
    }

    fun clearReportMedia() {
        reportMedia.clear()
    }

    fun updateReport(title: String, body: String, feedback: String) {
        this.title = title
        this.body = body
        this.feedback = feedback
        this.state = ReportState.PENDING
    }

    fun approve() {
        state = ReportState.APPROVED
    }

    fun reject(reason: String) {
        this.state = ReportState.REJECTED
        this.reason = reason
    }
}