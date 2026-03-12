package com.team1.mvp_test.domain.report.model

import jakarta.persistence.*

@Entity
@Table(name = "report_media")
class ReportMedia(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "media_url")
    var mediaUrl: String,
) {
}