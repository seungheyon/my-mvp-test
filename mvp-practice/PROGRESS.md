# MVP Practice 진행 상황

## 목적
`mvp-test` 프로젝트를 처음부터 다시 만들며 인터뷰 준비.
막히는 부분은 Claude에게 힌트 요청.

## 환경
- 브랜치: `practice`
- 포트: 8081
- DB: MySQL localhost:3306/mvpPractice (user: admin / pw: admin)
- ddl-auto: create

## 전체 구현 목표
```
mvp-practice/src/main/kotlin/com/team1/mvp_practice/
├── common/          # 공통 DTO, 예외, 에러 메시지
├── domain/
│   ├── member/      # 일반 회원
│   ├── enterprise/  # 기업 회원
│   ├── mvptest/     # MVP 테스트 (핵심 도메인)
│   ├── step/        # 테스트 단계
│   └── report/      # 테스트 보고서
└── infra/           # Security, QueryDSL, Redis 설정
```

## 진행 상황

### 완료
- [x] 프로젝트 기본 세팅 (build.gradle.kts, settings.gradle.kts, application.yml)
- [x] MvpPracticeApplication.kt

### 다음 작업
- [ ] IntelliJ에서 mvp-practice Gradle 프로젝트 import
- [ ] MySQL에 mvpPractice DB 생성
- [ ] 앱 실행 확인 (포트 8081)
- [ ] 도메인 설계 시작 (Member, Enterprise Entity부터)

## 메모
- 구현할 때마다 이 파일의 체크박스를 업데이트할 것
