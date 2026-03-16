# 백엔드 의사결정 기록

백엔드 파트에서 내린 주요 기술적 결정과 그 이유를 기록합니다.

---

## [001] 커서 기반 페이지네이션 도입

**날짜**: 2026-03-02
**관련 기능**: `GET /api/v1/mvp-tests` — MvpTest 목록 조회

### 결정
오프셋 기반 페이지네이션 대신 **커서 기반 페이지네이션**을 도입한다.

### 이유
- 오프셋 방식은 데이터 삽입/삭제 시 중복 노출 또는 누락이 발생한다.
- `OFFSET N`은 DB가 앞의 N개를 스캔한 뒤 버리는 방식이라, 데이터가 많아질수록 성능이 저하된다.
- 커서 방식(`WHERE id < cursor`)은 인덱스를 직접 활용하므로 데이터 양에 무관하게 일정한 성능을 보장한다.

### 구현 방식
- 커서: `id` (Long, auto-increment — 단조 증가로 정렬 기준에 적합)
- 정렬: `ORDER BY id DESC`
- `size + 1` 트릭으로 `hasNext`를 판단한다 (COUNT 쿼리 불필요)

---

## [002] 페이지네이션 조립 로직의 위치 — CursorPageResponse.of()

**날짜**: 2026-03-02
**관련 기능**: 커서 기반 페이지네이션 전반

### 결정
`hasNext` 판단과 `nextCursor` 추출 로직을 Service가 아닌
**`CursorPageResponse`의 companion object 팩토리 메서드 `of()`** 에 위치시킨다.

### 이유
- `hasNext` 판단과 커서 추출은 비즈니스 로직이 아닌 **페이지네이션 조립 로직**이다.
- Service에 두면 비즈니스 로직과 페이지네이션 인프라 로직이 섞여 관심사가 분리되지 않는다.
- `CursorPageResponse.of(items, size) { it.id }` 형태로 호출하면 Service는 조회 + 매핑에만 집중할 수 있다.
- 다른 도메인에서 커서 페이지네이션을 추가할 때도 동일한 `of()`를 재사용할 수 있다.

### 결과 구조
```
Repository  →  size+1개 조회
Service     →  조회 + DTO 매핑
CursorPageResponse.of()  →  hasNext 판단, nextCursor 추출, 리스트 트림
```

---

## [003] 기업용 Report 목록 조회 API 추가

**날짜**: 2026-03-15
**관련 기능**: `GET /api/v1/mvp-tests/{testId}/steps/{stepId}/reports`

### 결정
기업이 특정 스텝에 제출된 모든 리포트를 조회하는 API를 추가한다.

### 인가 설계
- `@PreAuthorize("hasRole('ENTERPRISE')")` 로 역할 검증
- `step.mvpTest.id == testId` 로 스텝이 해당 테스트에 속하는지 검증
- `step.mvpTest.enterpriseId == enterpriseId` 로 소유권 검증
- URL에 `testId`를 명시적으로 포함시켜 경로만으로 리소스 계층 구조를 표현

### N+1 예고
- `findAllByStepId(stepId)` 후 각 `Report.reportMedia` LAZY 로딩 → N+1 발생
- 별도 `@BatchSize` 실험을 통해 해결 방안 결정 예정

---

## [004] getReportListByStep에 @Transactional(readOnly = true) 추가

**날짜**: 2026-03-15
**관련 기능**: `ReportService.getReportListByStep()`

### 문제
`application.yml`에 `open-in-view: false` 설정으로 인해 JPA 세션이 Repository 메서드 호출 단위로만 열리고 닫힌다. `@Transactional` 없이 `findAllByStepId()` 호출 후 `ReportResponse.from(report)` 에서 `report.reportMedia` (LAZY) 에 접근하면 세션이 이미 닫혀 `LazyInitializationException` 이 발생한다.

### 결정
`@Transactional(readOnly = true)` 를 추가한다.

### readOnly = true를 선택한 이유
- 조회 전용 메서드이므로 dirty checking(변경 감지) 이 불필요하다.
- Hibernate가 flush 를 생략하고 스냅샷을 관리하지 않아 메모리·성능 이점이 있다.
- `jakarta.transaction.Transactional` 은 `readOnly` 속성을 지원하지 않으므로 `org.springframework.transaction.annotation.Transactional` 을 fully qualified name 으로 사용했다 (파일 내 기존 jakarta import와 충돌 방지).

---

## [005] JWT 인증 실패 응답 메시지 개선

**날짜**: 2026-03-15
**관련 파일**: `JwtAuthenticationFilter`, `CustomAuthenticationEntryPoint`

### 문제
1. `JwtAuthenticationFilter` 가 JWT 검증 실패를 `.onFailure` 없이 조용히 삼킴 → 실패 원인이 소멸
2. `CustomAuthenticationEntryPoint` 가 모든 인증 실패에 "JWT Verification failed" 를 하드코딩 → 토큰 없음, 만료, 서명 불일치 등 원인과 무관하게 동일한 메시지 반환

### 결정
- **Filter**: `.onFailure` 추가, 예외 타입별로 분류한 원인 문자열을 `request attribute("jwt-error")` 에 저장
- **EntryPoint**: attribute 유무로 JWT 오류 vs 일반 인증 오류를 구분하여 메시지 반환

### 보안 고려
`it.message` (라이브러리 예외 메시지 raw 노출) 대신 예외 타입별 통제된 메시지를 사용한다. JJWT 예외 메시지에는 만료 시각, 서명 알고리즘 등 공격자에게 유용한 정보가 포함될 수 있기 때문이다.

### 결과 메시지
| 상황 | 응답 메시지 |
|------|------------|
| 토큰 없음 | `Authentication required` |
| 토큰 만료 | `JWT error: Token has expired` |
| 서명 불일치 | `JWT error: Invalid token signature` |
| 기타 토큰 오류 | `JWT error: Invalid token` |

---

## [006] getReportListByStep N+1 문제 해결

**날짜**: 2026-03-15
**관련 기능**: `GET /api/v1/mvp-tests/{testId}/steps/{stepId}/reports`

### 문제
`findAllByStepId(stepId)` 호출 시 Report의 EAGER `@ManyToOne` 연관관계로 인해 N+1이 연쇄 발생.

- `report.memberTest` → 리포트마다 다른 MemberTest → N번 SELECT
- `memberTest.member` → MemberTest마다 다른 Member → N번 SELECT
- `report.reportMedia` → LAZY `@OneToMany` → N번 SELECT (별도 문제)

리포트 50건 기준 최대 1 + 50 + 50 + 50 + 1 = 152번 쿼리.

### 결정
두 가지를 조합해 해결한다.

1. **`@Query` JOIN FETCH** — `ReportRepository.findAllByStepId`에 JPQL `JOIN FETCH r.memberTest mt JOIN FETCH mt.member` 추가. `@ManyToOne` N+1을 쿼리 1번으로 해소.
2. **`@BatchSize(size = 100)`** — `Report.reportMedia`에 추가. LAZY 로딩 시 N번 SELECT → IN 쿼리 1번으로 해소.

### JOIN FETCH를 선택한 이유 (`memberTest`, `member`)
- `@ManyToOne` JOIN FETCH는 Report 1건 = row 1건을 유지하므로 페이지네이션 도입 시에도 LIMIT이 정확히 동작한다.
- 페이지네이션 + JOIN FETCH가 문제가 되는 케이스는 `@OneToMany` (컬렉션 소유 측) 조회 시 row가 곱으로 늘어나는 경우이며, 이 케이스와 무관하다.

### @BatchSize를 선택한 이유 (`reportMedia`)
- `reportMedia`는 `@OneToMany`이므로 JOIN FETCH 시 row 곱 문제가 발생한다.
- LAZY를 유지해 `reportMedia`가 필요 없는 다른 API(approve, delete 등)에서 불필요한 로딩을 방지한다.
- `@Transactional(readOnly = true)`로 세션을 유지하고, `@BatchSize`로 IN 쿼리 1번에 해소한다.

### 결과 쿼리 횟수
| 쿼리 | 횟수 |
|------|------|
| step 단건 조회 | 1번 |
| report + memberTest + member JOIN FETCH | 1번 |
| reportMedia IN 쿼리 (@BatchSize) | 1번 |
| **합계** | **3번** |
