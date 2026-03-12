package com.team1.mvp_test.domain.report.repository

import com.team1.mvp_test.domain.report.model.ReportMedia
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReportMediaRepository : JpaRepository<ReportMedia, Long> {

}
