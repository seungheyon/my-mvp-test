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

---

## [007] 복합 커서 페이지네이션 도입 — (sortDate, id) 조합

**날짜**: 2026-03-17
**관련 기능**: `GET /api/v1/mvp-tests/sorted` — testStartDate / testEndDate 기준 정렬 목록 조회

### 문제
단일 `id` 커서는 `id` 기준 정렬에서만 정확하다. `testStartDate` 기준 정렬을 요청하면 동일한 날짜를 가진 행이 여러 개 존재할 수 있다. 이 경우 커서가 날짜 단일 값이면 동일 날짜 구간에서 페이지가 끊겼을 때 **중복 노출** 또는 **데이터 누락**이 발생한다.

### 결정
`(sortDate, id)` 복합 커서를 사용한다.

```sql
WHERE (sort_column < cursorDate)
   OR (sort_column = cursorDate AND id < cursorId)
ORDER BY sort_column DESC, id DESC
LIMIT size + 1
```

- 첫 번째 조건 `sort_column < cursorDate`: 날짜가 다른 경우 날짜만으로 이전 페이지를 필터링
- 두 번째 조건 `sort_column = cursorDate AND id < cursorId`: 날짜가 같은 행들 사이에서 `id`가 tie-breaker 역할

### 응답 DTO 설계 — DateCursorPageResponse
기존 `CursorPageResponse` (`nextCursor: Long?`)로는 날짜 커서를 담을 수 없다. `nextCursorDate: LocalDateTime?`와 `nextCursorId: Long?`을 별도 필드로 가지는 `DateCursorPageResponse<T>`를 신규 생성한다.

- 클라이언트는 두 값을 다음 요청의 `cursorDate`, `cursorId`로 그대로 전달하면 된다.
- `of()` 팩토리 메서드에 `dateExtractor`, `idExtractor` 두 람다를 받아 Service는 조립 로직 없이 `of()` 한 번만 호출한다.

### MvpTestSortType enum에 QueryDSL 의존성을 두지 않은 이유
정렬 기준을 enum의 메서드로 캡슐화(`sortColumn()` → `DateTimePath<LocalDateTime>` 반환)하면 도메인 모델 레이어가 QueryDSL(인프라) 클래스를 직접 import하게 된다. enum은 도메인 모델이므로 인프라 의존성을 갖지 않아야 한다. Repository Impl에서 `when`으로 분기하는 방식으로 의존성 방향을 유지한다.

```kotlin
val sortColumn: DateTimePath<LocalDateTime> = when (sortBy) {
    MvpTestSortType.TEST_START_DATE -> mvpTest.testStartDate
    MvpTestSortType.TEST_END_DATE   -> mvpTest.testEndDate
}
```

---

## [008] Report 목록 조회 API에 커서 기반 페이지네이션 추가

**날짜**: 2026-03-17
**관련 기능**: `GET /api/v1/mvp-tests/{testId}/steps/{stepId}/reports`

### 문제
기존 `getReportListByStep`은 `findAllByStepId(stepId)`로 해당 스텝의 리포트 전체를 한 번에 조회한다. 모집 인원이 많아질수록 응답 크기와 응답 시간이 무제한으로 증가한다.

### 결정
기존 엔드포인트에 `cursor: Long?`, `size: Int` 파라미터를 추가한다. 신규 엔드포인트를 만들지 않는다.

- Report는 `id` 기준 정렬로 충분하다. 날짜처럼 중복 가능한 컬럼을 기준으로 정렬할 필요가 없으므로 단일 `id` 커서를 사용한다.
- 기존 `CursorPageResponse<T>`를 그대로 재사용한다.
- `cursor` 파라미터가 없으면 첫 페이지 조회로 동작하므로 하위 호환이 유지된다.

### 기존 @Query JPQL 대신 QueryDSL을 선택한 이유
`size + 1` 트릭으로 `hasNext`를 판단하려면 쿼리에서 직접 `LIMIT size+1`을 제어해야 한다. `@Query` JPQL은 `LIMIT`을 직접 쓸 수 없고 `Pageable`을 통해야 하는데, 이 경우 Spring의 페이지 계산 방식을 따르게 되어 `size+1` 트릭을 적용할 수 없다. QueryDSL의 `.limit((size + 1).toLong())`으로 직접 제어한다.

기존 `@Query` JOIN FETCH(`memberTest`, `member`)는 그대로 유지하고, 새 QueryDSL 메서드가 동일한 JOIN FETCH + 커서 조건을 함께 담당한다.

---

## [009] GlobalExceptionHandler catch-all 추가

**날짜**: 2026-03-17
**관련 파일**: `GlobalExceptionHandler`

### 문제
`GlobalExceptionHandler`에 명시되지 않은 예외(예: `MissingServletRequestParameterException`)가 발생하면 Spring이 `/error`로 포워딩한다. `/error` 경로가 SecurityConfig의 `permitAll()` 목록에 없으므로 `AuthenticationEntryPoint`가 호출되어 **401**이 반환된다. 클라이언트 입장에서는 파라미터 누락이 인증 오류로 보이는 혼란이 생긴다.

### 결정
두 가지를 추가한다.

1. **`MissingServletRequestParameterException` → 400**: 클라이언트 실수(필수 파라미터 누락)이므로 400으로 명시 처리
2. **`Exception` catch-all → 500**: 명시되지 않은 모든 예외를 500으로 처리. 클라이언트에는 `"Internal server error"`만 노출하고, 서버 로그에는 스택트레이스를 기록한다

---

## [010] MvpTest 단건 조회에 캐시 적용 — 대상 선정과 캐싱 단위 결정

**날짜**: 2026-03-17
**관련 기능**: `GET /api/v1/mvp-tests/{testId}`

### 캐시 적용 대상 선정

아래 두 기준으로 이 프로젝트에서 캐시 적용이 유효한 API를 판단했다.

1. **읽기가 쓰기보다 압도적으로 많은가** — 쓰기가 잦으면 캐시 무효화가 빈번해져 복잡도만 증가
2. **데이터가 약간 stale해도 괜찮은가** — 실시간 정확성이 중요하면 캐시가 맞지 않음

`MvpTest` 단건 조회는 두 조건을 모두 만족한다. 테스트가 공개(`APPROVED`)되면 불특정 다수 멤버가 반복 조회하고, 기업이 한 번 등록하면 수정이 드물며, 정보가 몇 분 늦게 반영돼도 서비스에 큰 영향이 없다.

목록 조회(`GET /api/v1/mvp-tests`)는 캐시 키가 `(cursor, size, sortBy, ...)` 조합이 되어 경우의 수가 사실상 무한대다. 캐시에 저장은 되지만 재사용될 가능성이 거의 없어 캐시 히트율 ≈ 0 — 메모리만 낭비하는 구조가 되므로 제외한다.

### 캐싱 단위 — MvpTestResponse 전체

`MvpTest` 엔티티만 캐싱하고 연관 데이터(Enterprise, categories)를 별도로 조회하는 방식도 가능하지만 선택하지 않는다.

- Redis도 네트워크를 통한 원격 호출이다. 캐시 조회를 3번으로 나누면 Redis IO가 3번 발생해 레이턴시 절감 효과가 희석된다.
- `getMvpTest` 에서 세 데이터는 항상 함께 사용된다. 같은 요청에서 항상 함께 쓰이는 데이터는 하나의 키로 묶는 것이 맞다.

따라서 완성된 응답 DTO인 `MvpTestResponse` 전체를 단일 키로 캐싱한다. 캐시 히트 시 DB 조회 0번.

### 무효화 전략 — TTL + 삭제 시 즉시 Evict

`MvpTestResponse`는 `enterpriseName`, `categories` 같은 연관 객체를 포함한다. 연관 객체가 변경될 경우 캐시와 DB 간 정합성이 깨질 수 있다.

| 전략 | 설명 | 적용 |
|------|------|------|
| TTL 자연 만료 | 일정 시간 후 자동 갱신 | **업데이트** — 연관 객체 변경 드물고 stale 허용 가능 |
| @CacheEvict | 변경 시점에 즉시 무효화 | **삭제** — 존재하지 않는 리소스가 캐시에서 반환되는 것은 stale과 성격이 다름 |

업데이트는 "오래된 값"이지만 삭제는 "없어야 할 값"이 남아 있는 것이다. 삭제된 MvpTest가 5분간 정상 응답으로 반환되면 클라이언트 혼란이 크므로 `deleteMvpTest`에 `@CacheEvict`를 추가한다.
