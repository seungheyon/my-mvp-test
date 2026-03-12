package com.team1.mvp_test.common.error

enum class MemberErrorMessage(val message: String) {
    EMAIL_ALREADY_IN_USE("이미 사용중인 이메일입니다."),
    PHONE_NUMBER_ALREADY_IN_USE("이미 사용중인 번호입니다."),
    PHONE_NUMBER_VERIFY_FAILED("인증에 실패한 번호입니다."),
    NOT_ACTIVE("가입 완료된 계정이 아닙니다."),
    SIGN_UP_ALREADY_DONE("이미 가입이 완료된 계정입니다.")

}