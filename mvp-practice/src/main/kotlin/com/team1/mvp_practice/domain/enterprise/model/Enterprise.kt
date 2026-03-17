package com.team1.mvp_practice.domain.enterprise.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Enterprise(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name="email")
    val email: String,

    @Column(name="name")
    var name: String,

    @Column(name="ceo_name")
    var ceoName: String,

    @Column(name="password")
    var password: String,

    @Column(name="phone_number")
    var phoneNumber: String,

    @Column(name="state")
    var state: String,

    @Column(name="reason")
    var reason: String? = null,
) {


}