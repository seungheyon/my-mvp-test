package com.team1.mvp_test.domain.category.model

import jakarta.persistence.*

@Table(name = "category")
@Entity
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name")
    val name: String
) {

}