# CLAUDE.md

Claude가 이 프로젝트에서 작업할 때 따르는 규칙과 컨텍스트.

---

## 프로젝트 개요

MVP 테스트 플랫폼. 기업(Enterprise)이 MVP를 등록하고 멤버(Member)를 모집해 테스트를 진행하는 서비스.

---

## 기술 스택

| 항목 | 내용 |
|------|------|
| 언어 | Kotlin 1.9 |
| 프레임워크 | Spring Boot 3.3 |
| ORM | Spring Data JPA + QueryDSL 5.0 |
| DB | MySQL (운영), H2 / PostgreSQL (테스트) |
| 인증 | JWT + Spring Security |
| 캐시 / 락 | Redis, Redisson |
| 스토리지 | AWS S3 |
| 테스트 | Kotest, MockK, SpringMockK |

---

## 프로젝트 구조

```
mvp-test/src/main/kotlin/com/team1/mvp_test/
├── admin/          # 관리자 도메인
├── batch/          # 배치 작업 (정산 등)
├── common/         # 공통 DTO, 예외, 에러 메시지
├── domain/
│   ├── category/   # 카테고리
│   ├── enterprise/ # 기업 회원
│   ├── member/     # 일반 회원
│   ├── mvptest/    # MVP 테스트 (핵심 도메인)
│   ├── report/     # 테스트 보고서
│   ├── settlement/ # 정산
│   └── step/       # 테스트 단계
└── infra/          # 인프라 설정 (QueryDSL, Redis, S3, Security 등)
```

---

## 핵심 개발 규칙

### 1. 구현 전 이유 설명 (필수)
**모든 구현에 대해서는 사용자에게 이유를 설명한다.**
- 코드를 작성하거나 수정하기 전, 왜 이 방식을 선택했는지 먼저 설명한다.
- 대안이 존재하면 각 방식의 트레이드오프를 함께 설명한다.
- 설명 없이 코드만 작성하지 않는다.

### 2. 최소 변경 원칙
- 요청된 범위를 넘는 리팩토링이나 추가 개선을 하지 않는다.
- 새 파일 생성보다 기존 파일 수정을 우선한다.
- 변경하지 않은 코드에 주석, 타입 어노테이션 등을 임의로 추가하지 않는다.

### 3. 보안
- SQL 인젝션, XSS 등 OWASP Top 10 취약점을 만들지 않는다.
- 시스템 경계(사용자 입력, 외부 API)에서만 유효성 검사를 수행한다.

---

## QueryDSL 규칙

- 커스텀 쿼리는 `QueryDslRepository` 인터페이스에 선언하고 `Impl` 클래스에 구현한다.
- 복잡한 조건은 `BooleanBuilder`를 사용한다.
- Q클래스는 kapt로 자동 생성되며 `build/generated/source/kapt/main` 에 위치한다.

## 페이지네이션 규칙

- 목록 조회에는 **커서 기반 페이지네이션**을 사용한다.
- 커서 기준: `id` (Long), 정렬: 내림차순 (`ORDER BY id DESC`)
- `size + 1` 트릭으로 `hasNext`를 판단한다 (COUNT 쿼리 금지).
- 공통 응답 DTO: `common/dto/CursorPageResponse<T>`

---

## 도메인 핵심 규칙

- `MvpTest`의 상태: `PENDING` → `APPROVED` / `REJECTED`
- 모집 방식: `FIRST_COME`(선착순, Redisson 락 사용), `SELECTION`(선발)
- 기업은 `APPROVED` 상태여야 MVP 테스트를 등록할 수 있다.
- 멤버는 `ACTIVE` 상태여야 테스트에 지원할 수 있다.
