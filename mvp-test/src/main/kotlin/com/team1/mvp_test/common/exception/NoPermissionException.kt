package com.team1.mvp_test.common.exception

data class NoPermissionException(override val message: String) : RuntimeException()
