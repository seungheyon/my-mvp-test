package com.team1.mvp_test.admin.repository

import com.team1.mvp_test.admin.model.Admin
import org.springframework.data.jpa.repository.JpaRepository

interface AdminRepository : JpaRepository<Admin, Long> {
    fun findByAccount(account: String): Admin?
}