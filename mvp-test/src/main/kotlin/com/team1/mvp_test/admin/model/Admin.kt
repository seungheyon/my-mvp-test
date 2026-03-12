package com.team1.mvp_test.admin.model

import jakarta.persistence.*

@Entity
@Table(name = "admin")
class Admin(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "account", unique = true)
    val account: String,

    @Column(name = "password")
    val password: String,
)