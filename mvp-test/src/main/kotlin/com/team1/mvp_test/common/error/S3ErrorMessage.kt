package com.team1.mvp_test.common.error

enum class S3ErrorMessage(val message: String) {
    FILE_TYPE_NOT_VALID("해당 파일 형식은 지원 하지 않습니다."),
    EXCEED_MAX_FILE_SIZE("업로드 파일은 10MB를 초과 할 수 없습니다.")
}