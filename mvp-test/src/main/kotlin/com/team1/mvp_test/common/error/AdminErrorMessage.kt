package com.team1.mvp_test.common.error

enum class AdminErrorMessage(val message: String) {
    ALREADY_APPROVED_TEST("이미 승인된 테스트입니다."),
    ALREADY_REJECTED_TEST("이미 거절된 테스트입니다."),
    ALREADY_APPROVED_ENTERPRISE("이미 승인된 기업입니다."),
    ALREADY_REJECTED_ENTERPRISE("이미 회원가입이 거절된 기업입니다."),
    ALREADY_BLOCKED_ENTERPRISE("이미 제재당한 기업입니다."),
    ALREADY_APPROVED_MEMBER("이미 승인된 사용자입니다."),
    ALREADY_BLOCKED_MEMBER("이미 제재당한 사용자입니다."),
}