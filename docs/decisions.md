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
