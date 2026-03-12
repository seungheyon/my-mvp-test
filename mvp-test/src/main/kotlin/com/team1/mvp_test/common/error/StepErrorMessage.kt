package com.team1.mvp_test.common.error

enum class StepErrorMessage(var message: String) {
    NOT_AUTHORIZED("접근 권한이 없습니다."),
    NOT_APPROVED_TEST("승인되지 않은 테스트에 스탭 생성은 불가능합니다.")
}