package com.team1.mvp_test.common.error

enum class EnterpriseErrorMessage(val message: String) {
    ALREADY_EXISTS("이미 존재하는 기업입니다."),
    NOT_AUTHORIZED("테스트 작성자가 아닙니다."),
    PENDING_STATE("가입 승인이 대기 중입니다."),
    REJECTED_STATE("가입 거절 된 기업입니다.")
}