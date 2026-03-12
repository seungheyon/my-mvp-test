package com.team1.mvp_test.common.error

enum class ReportErrorMessage(val message: String) {
    NOT_AUTHORIZED("보고서를 작성한 사용자가 아닙니다."),
    NOT_TEST_DURATION("테스트 작성 기간이 아닙니다."),
    NO_PERMISSION("보고서 작성 권한이 없습니다."),
    NOT_YOUR_TEST("테스트를 작성한 기업이 아닙니다."),
    MEDIA_COUNT_OVER("최대 10개의 미디어만 업로드 할 수 있습니다"),
    TITLE_LENGTH_INCORRECT("TITLE 을 5~50 자 이내로 입력해 주세요"),
    CONTENT_LENGTH_INCORRECT("BODY, FEEDBACK, REASON 을 5~500 자 이내로 입력해 주세요"),
    ALREADY_APPROVED("이미 승인된 보고서 입니다"),
    ALREADY_REPORTED("이미 보고서를 작성 했습니다"),
    REPORT_MEDIA_URL_NOT_EXIST("보고서에 사진 파일은 필수입니다."),
    REPORT_MEDIA_URL_COUNT_NOT_VALID("보고서에 사진 파일은 최대 10개 업로드 가능합니다.")

}