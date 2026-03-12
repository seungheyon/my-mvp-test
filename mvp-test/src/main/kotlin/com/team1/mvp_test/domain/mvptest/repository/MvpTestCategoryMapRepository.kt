package com.team1.mvp_test.domain.mvptest.repository

import com.team1.mvp_test.domain.mvptest.model.MvpTestCategoryMap
import org.springframework.data.jpa.repository.JpaRepository

interface MvpTestCategoryMapRepository : JpaRepository<MvpTestCategoryMap, Long> {
    fun findAllByMvpTestId(mvpTestId: Long): List<MvpTestCategoryMap>
}