# my-mvp-test

MVP 테스트 플랫폼 코드베이스를 재료로 **백엔드 설계·성능 주제를 개인 학습**한 저장소.
기능을 새로 만드는 것보다, 이미 동작하는 코드에서 문제를 찾아 고치고 그 판단 근거를 문서로 남기는 데
초점을 뒀다.

## 출처와 기여 범위

이 저장소의 애플리케이션 코드(`mvp-test/`)는 **팀 프로젝트에서 가져온 것**이다.

- 원본: [MVP-Test-FinalProject/mvp-test](https://github.com/MVP-Test-FinalProject/mvp-test)
  (Kotlin / Spring Boot, 5인 팀 프로젝트. 팀원 구성은 [`mvp-test/README.md`](mvp-test/README.md) 참고)
- 원본 코드는 커밋 `6e83401`에서 178개 파일이 한 번에 임포트됐다. **그 커밋 이전의 코드는 팀의 작업물이다.**
- 이 저장소에서 개인적으로 한 작업은 그 이후의 커밋 14건이다.

원본 저장소에는 별도 라이선스가 명시돼 있지 않다. 재사용을 고려한다면 원본 저장소를 먼저 확인할 것.

## 이 저장소에서 한 작업

| 주제 | 내용 |
|---|---|
| 페이지네이션 | QueryDSL 커서 기반 페이지네이션 도입 → 단일 `id` 커서의 한계(중복 가능 컬럼 정렬 시 누락/중복) 확인 → `(sortDate, id)` 복합 커서로 재설계. Report 목록 조회에도 적용 |
| N+1 | 기업용 Report 목록 조회 API 추가 과정에서 `@ManyToOne` N+1을 발견하고 JOIN FETCH로 해결. BatchFetching 적용 가능 지점 정리 |
| 캐시 | MvpTest 단건 조회 캐시 적용 → Redis 직렬화 오류 수정(`jackson-module-kotlin` 추가, `DefaultTyping.EVERYTHING`) → Cache Penetration 방지를 위한 null 캐싱 |
| 예외 처리 | 명시되지 않은 예외가 401로 반환되던 원인 추적, `GlobalExceptionHandler` catch-all 추가, JWT 인증 실패 응답 메시지 개선 |

## 문서

작업의 결과물은 코드보다 이쪽에 더 많다.

- [`mvp-test/INTERVIEW_NOTES.md`](mvp-test/INTERVIEW_NOTES.md) (1,300여 줄) — 위 주제들을 "왜 이렇게 했는가"
  관점으로 정리한 노트. 연관관계 트레이드오프, JPA 없이 도메인 관계를 구현한다면, `open-in-view: false`
  환경의 트랜잭션과 LAZY 로딩, 복합 커서와 인덱스 전제 조건, 애그리거트 루트 혼동 정정 등
- [`docs/decisions.md`](docs/decisions.md) — 결정 [001]~[010]. 각 항목에 날짜·관련 기능·결정·근거를 남김
- [`CLAUDE.md`](CLAUDE.md) — 이 저장소에서 쓴 작업 규칙과 프로젝트 컨텍스트

## 구성

```
mvp-test/       원본 팀 프로젝트 코드 + 위 개선 작업
mvp-practice/   같은 도메인을 백지에서 다시 구현해 보는 별도 Gradle 프로젝트 (초기 단계)
docs/           의사결정 기록
```

## 스택

Kotlin 1.9 / Spring Boot 3.3 / Spring Data JPA + QueryDSL 5.0 / MySQL(운영)·H2(테스트) /
JWT + Spring Security / Redis · Redisson / AWS S3 / Kotest · MockK
