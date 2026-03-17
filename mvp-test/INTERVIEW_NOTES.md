# 인터뷰 준비 노트 (mvp-test)

기능 추가, 실험, 성능 테스트를 진행하면서 나온 고민, 선택 이유, 트레이드오프를 기록.

---

## 목차

- [도메인 모델링](#도메인-모델링)
  - 연관관계를 맺는 세 가지 방식과 그 트레이드오프
  - JPA 없이 도메인 간 관계를 구현한다면?
  - N+1 문제 — 발생과 구현의 차이, 그리고 해결 방법
- [N+1과 BatchFetching](#n1과-batchfetching)
  - 이 프로젝트에서 BatchFetching이 적용 가능한 지점
  - getReportListByStep — @ManyToOne N+1 발견과 JOIN FETCH 해결
  - @ManyToOne EAGER — 단건 vs 컬렉션 조회, 그리고 1차 캐시
- [API 설계](#api-설계)
  - 리소스 계층 구조와 URL 설계
  - open-in-view: false 환경에서의 트랜잭션과 LAZY 로딩
  - 애그리거트 루트와 @OneToMany 혼동 정정
- [페이지네이션](#페이지네이션)
  - 단일 id 커서의 한계 — 중복 가능한 컬럼 정렬 시 문제
  - 복합 커서 설계 — (sortDate, id) 조합
  - BooleanBuilder로 OR 조건 표현
  - MvpTestSortType enum에 QueryDSL 의존성을 두지 않는 이유
  - Report 목록 조회 — 기존 API 수정 vs 신규 엔드포인트
- [예외 처리](#예외-처리)
  - GlobalExceptionHandler catch-all 패턴
  - 명시되지 않은 예외가 401로 반환되는 원인
- [캐시](#캐시)
  - 캐시 적용 기준 — 어떤 API에 붙일 것인가
  - 응답 전체 캐싱 vs 엔티티별 분리 캐싱
  - 연관 객체 변경 시 정합성 문제와 무효화 전략

---

## 도메인 모델링

### 연관관계를 맺는 세 가지 방식과 그 트레이드오프

이 프로젝트의 엔티티 관계를 보면 세 가지 방식이 혼재한다.

---

#### 1. FK only — 외래키 숫자만 보관

```kotlin
// MvpTest.kt
val enterpriseId: Long  // Enterprise 객체를 직접 참조하지 않음
```

**언제 쓰나**
`Enterprise`(기업)와 `MvpTest`(MVP 테스트), `Member`와 `MemberReward`처럼
**서로 다른 도메인에 속하는 엔티티 사이의 관계**에 사용한다.

**장점**

- **도메인 간 결합을 원천 차단한다.** `MvpTest`를 로딩해도 `Enterprise` 조회 쿼리가 절대 나가지 않는다. N+1 문제가 발생할 구조 자체가 없다.
- **트랜잭션 경계가 명확해진다.** Enterprise 서비스와 MvpTest 서비스는 서로의 영속성 컨텍스트를 모른다. 나중에 MSA로 분리하더라도 코드 변경이 최소화된다.
- **JPA 연관관계 관리 부담이 없다.** `cascade`, `fetch`, `orphanRemoval` 등 설정 실수로 인한 사이드 이펙트를 피할 수 있다.

**단점**

- **조인 쿼리를 애플리케이션에서 직접 작성해야 한다.** "MVP 테스트 목록에 기업명도 함께 보여줘" 같은 요구사항이 생기면 QueryDSL로 직접 join을 작성해야 한다.
- **JPA가 참조 무결성을 보장하지 않는다.** 존재하지 않는 `enterpriseId`를 저장해도 컴파일, 런타임 에러가 발생하지 않는다. DB의 FK 제약 조건이나 서비스 레이어의 검증 로직으로 직접 방어해야 한다.
- **객체 그래프 탐색이 불가능하다.** `mvpTest.enterprise.name` 처럼 체이닝으로 접근할 수 없고, 별도 조회가 필요하다.

---

#### 2. 객체 참조 (@ManyToOne)

```kotlin
// Step.kt
@ManyToOne
@JoinColumn(name = "mvp_test_id")
val mvpTest: MvpTest
```

**언제 쓰나**
`Step`이 `MvpTest`에 속하고, `Report`가 `Step`과 `MemberTest`를 알아야 하는 것처럼
**같은 도메인 내에서 소유 관계가 명확한 경우**에 사용한다.

**장점**

- **객체 그래프 탐색이 자연스럽다.** `step.mvpTest.recruitEndDate` 처럼 연관된 엔티티의 필드에 직접 접근할 수 있다.
- **JPA가 참조 무결성을 보장한다.** 존재하지 않는 `MvpTest`를 연결하려 하면 런타임에 에러가 발생한다.
- **QueryDSL 조인이 편리하다.** `QStep.step.mvpTest.state` 처럼 Q클래스를 통해 자연스럽게 조인 조건을 표현할 수 있다.

**단점**

- **N+1 문제를 항상 경계해야 한다.** `FetchType.LAZY`(기본값)로 설정해 두어도, 반복문에서 `.mvpTest`에 접근하는 순간 N번의 쿼리가 추가로 나간다. `fetch join`이나 `@EntityGraph`로 의식적으로 해결해야 한다.
- **양방향 설정 시 복잡도가 크게 증가한다.** `@OneToMany` + `@ManyToOne`을 양방향으로 설정하면 `mappedBy` 설정, `cascade` 범위 결정, Jackson 직렬화 시 무한 순환 참조 방지(`@JsonManagedReference` 등) 등 관리 포인트가 늘어난다.
- **도메인 간 경계를 흐릴 수 있다.** 참조가 편리하다는 이유로 서로 다른 도메인의 엔티티에도 객체 참조를 걸기 시작하면, 의도하지 않은 곳에서 연쇄 쿼리가 발생하고 도메인 독립성이 무너진다.

---

#### 3. 매핑 엔티티 — 연결 테이블을 객체로 승격

```kotlin
// MemberTest.kt — Member × MvpTest 다대다 관계 + 지원 상태
@ManyToOne @JoinColumn(name = "member_id") val member: Member
@ManyToOne @JoinColumn(name = "test_id")   val test: MvpTest
val state: MemberTestState  // PENDING, APPROVED, REJECTED
```

**언제 쓰나**
다대다(M:N) 관계를 표현해야 할 때 JPA의 `@ManyToMany` 대신 사용한다.
`MemberTest`(멤버의 테스트 지원)와 `MvpTestCategoryMap`(테스트-카테고리 매핑)이 여기에 해당한다.

**왜 `@ManyToMany`를 쓰지 않나**

JPA의 `@ManyToMany`는 중간 조인 테이블을 JPA가 내부적으로 관리하기 때문에 제어권이 없다.
`cascade = ALL`로 설정된 컬렉션에서 실수로 `clear()`를 호출하면 연관된 데이터가 전부 삭제된다.
또한 중간 테이블에 컬럼을 추가(예: 지원 상태, 지원 시각)하려면 어차피 엔티티로 승격해야 한다.

**장점**

- **관계 자체에 비즈니스 데이터를 담을 수 있다.** `MemberTest`의 `state`(지원 상태)처럼, 두 엔티티의 연결 그 자체가 의미 있는 상태나 속성을 갖는 경우를 자연스럽게 표현한다.
- **`@ManyToMany`의 위험성을 제거한다.** 중간 테이블에 대한 완전한 제어권을 가지므로, cascade 실수로 인한 데이터 전체 삭제 위험이 없다.
- **조회 유연성이 높다.** "이 테스트에 APPROVED된 멤버 목록"을 `MemberTest`를 직접 쿼리해서 가져올 수 있다.

**단점**

- **엔티티와 리포지토리 수가 늘어난다.** 관계마다 클래스 파일과 리포지토리가 추가되므로 코드량이 증가한다.
- **삽입·삭제를 직접 관리해야 한다.** `@ManyToMany`의 컬렉션에 추가/제거하는 방식이 아니라, `memberTestRepository.save(MemberTest(...))` 처럼 중간 엔티티를 직접 생성하고 저장해야 한다.

---

#### 이 프로젝트의 설계 기준 정리

| 관계 | 방식 | 이유 |
|------|------|------|
| Enterprise → MvpTest | FK only | 도메인 경계 분리. 기업 도메인과 테스트 도메인은 독립적으로 동작해야 함 |
| MvpTest → Step | 객체 참조 | 같은 도메인 내 강한 소유 관계. Step은 MvpTest 없이 존재할 수 없음 |
| Step → Report | 객체 참조 | 동일 이유. Report는 특정 Step의 수행 결과물 |
| Member × MvpTest | 매핑 엔티티 (MemberTest) | 지원 상태라는 비즈니스 의미가 있고, `@ManyToMany` 위험 회피 |
| MvpTest × Category | 매핑 엔티티 (MvpTestCategoryMap) | `@ManyToMany` 위험 회피. 추후 매핑 속성 추가 여지 |

**한 줄로 요약하면:**
> 도메인 경계를 넘는 관계는 FK only, 같은 도메인 내 소유 관계는 객체 참조, 다대다는 매핑 엔티티로 표현한다.

---

### JPA 없이 도메인 간 관계를 구현한다면?

JPA의 `@ManyToOne`은 사실 두 가지를 동시에 해준다.

1. **객체 그래프 탐색** — `step.mvpTest.name` 처럼 연관 객체에 직접 접근
2. **데이터 페치 자동화** — 접근하는 순간 DB 조회 쿼리를 대신 실행

JPA가 없다면 이 두 가지를 직접 분리해서 구현해야 한다. 선택지는 크게 세 가지다.

---

#### 방법 1: ID 참조 + 명시적 Repository 조회

```kotlin
// 엔티티는 ID만 보관
data class Step(
    val id: Long,
    val mvpTestId: Long,   // 객체 참조 대신 ID만
    val title: String,
    val reward: Int
)

// 서비스에서 필요할 때 직접 조회
class StepService(
    private val stepRepository: StepRepository,
    private val mvpTestRepository: MvpTestRepository
) {
    fun getStepWithTest(stepId: Long): StepWithTestResponse {
        val step = stepRepository.findById(stepId)
        val mvpTest = mvpTestRepository.findById(step.mvpTestId) // 명시적 조회
        return StepWithTestResponse(step, mvpTest)
    }
}
```

JPA의 FK only 패턴과 구조가 완전히 동일하다. JPA가 없으면 이 방식이 가장 기본적인 선택이고, DDD에서 **애그리거트 간 참조는 ID로만** 한다는 원칙이 여기서 비롯된다.

오히려 JPA의 지연 로딩보다 코드 흐름이 더 투명하다는 장점도 있다. 쿼리가 발생하는 시점이 코드에 명시적으로 드러나기 때문에 N+1이 발생하더라도 즉시 눈에 보인다.

---

#### 방법 2: 도메인 서비스가 두 도메인을 조립

두 도메인 객체 어느 쪽도 상대방을 직접 알면 안 될 때, 제3의 도메인 서비스가 중간에서 조립을 담당한다.

```kotlin
// EnterpriseService도, MvpTestService도 서로를 모름
// 둘을 조합해야 하는 비즈니스 규칙은 별도 서비스가 담당

class MvpTestApprovalService(
    private val enterpriseRepository: EnterpriseRepository,
    private val mvpTestRepository: MvpTestRepository
) {
    fun approve(mvpTestId: Long) {
        val mvpTest = mvpTestRepository.findById(mvpTestId)
        val enterprise = enterpriseRepository.findById(mvpTest.enterpriseId)

        // enterprise 상태 검증 후 mvpTest 승인 처리
        check(enterprise.state == EnterpriseState.APPROVED) { "기업이 승인되지 않았습니다." }
        mvpTestRepository.save(mvpTest.approve())
    }
}
```

각 도메인 객체는 상대방을 전혀 모르면서도, "기업이 APPROVED여야 테스트를 등록할 수 있다"는 비즈니스 요구사항을 처리할 수 있다. 비즈니스 규칙을 어느 한 쪽 도메인에 우겨넣지 않아도 된다는 점이 핵심이다.

---

#### 방법 3: 도메인 이벤트로 결합 없이 반응

두 도메인이 서로를 전혀 모르게 하면서도 한쪽의 변화가 다른 쪽에 연쇄 작용해야 할 때 사용한다.

```kotlin
// Enterprise 도메인은 이벤트만 발행, MvpTest를 전혀 모름
class Enterprise(val id: Long, val state: EnterpriseState) {
    fun block(): EnterpriseBlockedEvent {
        return EnterpriseBlockedEvent(enterpriseId = this.id)
    }
}

// MvpTest 도메인은 이벤트를 구독해서 반응
class MvpTestEventHandler(
    private val mvpTestRepository: MvpTestRepository
) {
    fun on(event: EnterpriseBlockedEvent) {
        // 기업이 차단되면 해당 기업의 모든 테스트를 자동 중단
        val tests = mvpTestRepository.findByEnterpriseId(event.enterpriseId)
        tests.forEach { it.suspend() }
        mvpTestRepository.saveAll(tests)
    }
}
```

**의존 방향이 역전**되는 것이 핵심이다. Enterprise는 MvpTest를 모르고, MvpTest 쪽이 Enterprise의 이벤트를 구독하는 구조다. 두 도메인 사이의 유일한 공유 지점은 이벤트 객체(`EnterpriseBlockedEvent`) 하나뿐이다.

---

#### 세 방법의 트레이드오프 비교

| | ID 참조 + 명시적 조회 | 도메인 서비스 조립 | 도메인 이벤트 |
|---|---|---|---|
| **결합도** | 낮음 (ID만 공유) | 낮음 (양쪽 모름) | 가장 낮음 (이벤트만 공유) |
| **복잡도** | 낮음 | 중간 | 높음 (이벤트 버스, 핸들러 등) |
| **디버깅** | 쉬움 | 중간 | 어려움 (흐름이 비선형) |
| **사용 시점** | 단순 조회·연산 | 두 도메인을 조합하는 비즈니스 규칙 | 도메인 간 연쇄 부작용 처리 |

**결론:**
JPA의 `@ManyToOne`은 "필요할 때 알아서 조회해줄게"라는 편의 기능이다. 그 편의 뒤에는 항상 누군가 DB에서 데이터를 가져오는 행위가 있고, JPA가 없으면 그 행위를 서비스 코드에서 명시적으로 작성해야 한다. 어떤 방법을 쓰든 핵심 원칙은 같다 — **도메인 객체는 ID로만 상대방을 참조하고, 실제 조회와 조립은 서비스 레이어가 담당한다.**

---

### N+1 문제 — 발생과 구현의 차이, 그리고 해결 방법

#### "N+1 문제가 발생했다"는 것의 의미

N+1을 논할 때 두 가지를 명확히 구분해야 한다.

- **N개의 연관 데이터를 읽어야 한다** — 이것은 요구사항이 강제하는 것이다. MvpTest 목록에 기업명과 카테고리를 함께 보여줘야 한다면, 그 데이터는 반드시 어디선가 읽어야 한다. 이것은 필연이다.
- **N번의 개별 쿼리로 읽는다** — 이것은 구현 방식의 선택이다. 같은 데이터를 1번의 IN 쿼리나 JOIN으로 가져올 수도 있다. N+1 "문제"라고 부르는 것은 바로 이 패턴이다.

즉, N+1 문제란 "N개의 데이터를 읽는 것" 자체가 아니라 "그것을 N번의 별도 DB 왕복으로 처리하는 것"이다. N번 왕복이 성능에 영향을 주는 이유는 데이터를 N개 읽어서가 아니라, **네트워크 왕복 비용이 N번 발생**하기 때문이다.

#### 현재 코드에서의 상황

```kotlin
fun getMvpTestList(cursor: Long?, size: Int): CursorPageResponse<MvpTestResponse> {
    val items = mvpTestRepository.findMvpTestListByCursor(cursor, size)  // 쿼리 1번
        .map { mvpTest ->
            val enterprise = enterpriseRepository.findByIdOrNull(mvpTest.enterpriseId)!!  // N번
            val categories = mvpTestCategoryMapRepository.findAllByMvpTestId(mvpTest.id!!)  // N번
                .map { it.category.name }
            MvpTestResponse.from(mvpTest, enterprise, categories)
        }
}
```

이 코드는 JPA lazy loading의 함정에 빠진 것이 아니라, **N+1 패턴을 서비스 레이어에서 직접 코드로 구현한 것**이다. JPA가 뒤에서 몰래 하던 걸 눈에 보이게 작성했다는 점에서 오히려 투명하지만, 그렇다고 문제가 없는 것은 아니다. size=10 요청 시 최대 23번의 쿼리가 발생한다 (1 + 11 + 11).

#### JPA 연관관계가 없을 때 N+1 해결 방법

현재 구조는 FK only와 매핑 엔티티로 이루어져 있어 JPA의 `fetch join`이나 `@BatchSize` 같은 메커니즘을 사용할 수 없다.

- **`fetch join`** — `@ManyToOne`으로 연결된 객체 참조가 있어야 `JOIN FETCH mvpTest.enterprise` 형태로 사용 가능한데, `enterpriseId: Long` FK only에는 그 참조 경로 자체가 없다.
- **`@BatchSize`** — JPA가 관리하는 lazy proxy에 훅을 걸어 IN 쿼리로 묶어주는 방식인데, 직접 repository를 호출하는 코드에는 JPA가 개입할 여지가 없으므로 동작하지 않는다.

따라서 이 구조에서의 해결 방법은 아래 두 가지다.

| 방법 | 쿼리 횟수 | 특징 |
|---|---|---|
| **IN 쿼리로 일괄 조회** | 3번 | 구조 변경 최소. ID 목록을 수집한 뒤 `findAllById(ids)`로 한 번에 조회하고, 서비스에서 Map으로 매핑 |
| **QueryDSL DTO Projection** | 1번 | JOIN으로 한 방에 DTO로 받아옴. 가장 효율적이나 조회 컬럼 고정, 쿼리 복잡도 증가 |

#### 페이지네이션 환경에서의 추가 고려사항

`@OneToMany` 관계에서 `fetch join`을 페이지네이션과 함께 사용하면 문제가 생긴다. 예를 들어 MvpTest 1건에 카테고리가 3개면 JOIN 결과는 3 row가 되는데, 여기에 `LIMIT 10`을 걸면 MvpTest 10건이 아니라 row 10개가 된다. Hibernate는 이를 인지하고 DB에서 LIMIT을 처리하지 않고 **전체 결과를 메모리에 올린 뒤 애플리케이션에서 잘라내는** 방식으로 동작한다. 이는 페이지네이션의 목적을 무너뜨리고 OOM 위험까지 생긴다.

그래서 `@OneToMany` + 페이지네이션 조합에서는:
- **`@BatchSize`** — lazy 로딩을 IN 쿼리로 묶어 완화 (N → N/배치사이즈). 완전한 해결은 아님
- **DTO Projection** — 처음부터 JOIN + DTO로 받아서 완전히 해결

---

## N+1과 BatchFetching

### 이 프로젝트에서 BatchFetching이 적용 가능한 지점

이 프로젝트의 연관관계 전체 지도:

```
Enterprise ──── FK only ────→ MvpTest       (도메인 경계, 객체 참조 없음)
MvpTestCategoryMap ──@ManyToOne──→ MvpTest
MvpTestCategoryMap ──@ManyToOne──→ Category

Step ──@ManyToOne──→ MvpTest
Report ──@ManyToOne──→ Step
Report ──@ManyToOne──→ MemberTest

Report ──@OneToMany──→ ReportMedia          ← 유일한 @OneToMany
```

`Report.reportMedia`가 프로젝트 전체에서 `@OneToMany`가 달린 유일한 관계다. 이 지점이 JPA의 LAZY 로딩으로 인한 N+1이 실제로 발생하고, `@BatchSize`로 해결 가능한 곳이다.

#### N+1이 발생하는 경위

기업이 특정 스텝에 제출된 리포트 목록을 조회할 때:

```kotlin
// GET /api/v1/mvp-tests/{testId}/steps/{stepId}/reports
fun getReportListByStep(enterpriseId: Long, testId: Long, stepId: Long): List<ReportResponse> {
    ...
    return reportRepository.findAllByStepId(stepId)  // SELECT * FROM report WHERE step_id = ?  (쿼리 1번)
        .map { ReportResponse.from(it) }              // it.reportMedia 접근 → SELECT N번 발생
}
```

`ReportResponse.from(report)`에서 `report.reportMedia`에 접근하는 순간, 각 Report마다 LAZY 로딩 SELECT가 나간다. 리포트가 10건이면 1 + 10 = 11번 쿼리.

#### @BatchSize로 해결

```kotlin
// Report.kt
@OneToMany
@JoinColumn(name = "report_id")
@BatchSize(size = 100)
var reportMedia: MutableList<ReportMedia> = mutableListOf()
```

`@BatchSize(size = 100)`을 추가하면 JPA가 개별 SELECT 대신 `IN (?, ?, ...)` 쿼리로 묶어서 가져온다. 리포트가 100건 이하라면 1 + 1 = 2번 쿼리.

#### 왜 이 설계(@OneToMany)가 정당한가

Report와 ReportMedia는 **같은 도메인(`report/`) 내 소유 관계**다. ReportMedia는 Report 없이 단독으로 존재할 의미가 없는 첨부 파일 개념이다. Report가 애그리거트 루트이고, ReportMedia는 그 구성 요소이므로 `@OneToMany`는 자연스러운 선택이다. 인터뷰에서 "왜 이렇게 설계했나"라는 질문에 이 이유로 답할 수 있다.

#### 왜 fetch join 대신 BatchFetching인가

페이지네이션 환경에서 `@OneToMany`에 `fetch join`을 사용하면 Hibernate가 DB에서 LIMIT을 처리하지 않고 전체 결과를 메모리에 올린 뒤 애플리케이션에서 잘라낸다. 이는 OOM 위험을 만든다. `@BatchSize`는 이 문제 없이 IN 쿼리로 묶어주므로 페이지네이션 + `@OneToMany` 조합에 적합하다.

#### @BatchSize를 쓰면 LAZY를 EAGER로 바꿔도 되는가?

아니다. `@BatchSize`는 **언제** 로딩하느냐(LAZY/EAGER)가 아니라 **어떻게** 로딩하느냐(개별 SELECT → IN 쿼리)만 바꾼다.

EAGER로 바꾸면 `approveReport`, `deleteReport` 등 `reportMedia`가 필요 없는 메서드에서도 매번 IN 쿼리가 나간다. LAZY를 고수하는 이유는 "필요 없는 곳에서 컬렉션을 끌고 오지 않기 위해"서다.

따라서 세 가지는 각자의 역할이 있다:

| | 해결하는 문제 |
|---|---|
| `@Transactional` | 세션을 열어두어 LAZY 로딩 가능하게 함 |
| `@BatchSize` | LAZY 로딩 발생 시 N번 SELECT → IN 쿼리 1번으로 줄임 |
| `LAZY` 유지 | `reportMedia`가 필요 없는 메서드에서 불필요한 로딩 방지 |

---

### getReportListByStep — @ManyToOne N+1 발견과 JOIN FETCH 해결

#### 문제: reportMedia만 고려했다가 연쇄 N+1 발견

`@BatchSize`로 `reportMedia` N+1을 해결한 뒤 실제 쿼리 로그를 확인했더니 여전히 쿼리가 과다하게 나가고 있었다. 원인은 `Report`에 걸린 `@ManyToOne` 연관관계들이었다.

```
Report ──@ManyToOne──→ MemberTest  ← 리포트마다 다른 MemberTest → N번 SELECT
MemberTest ──@ManyToOne──→ Member  ← MemberTest마다 다른 Member → N번 SELECT
```

리포트 50건 기준 최대 쿼리 횟수:

```
1 (step 단건)
+ 1 (report 목록)
+ 50 (memberTest, 리포트마다 다른 MemberTest)
+ 50 (member, MemberTest마다 다른 Member)
+ 1 (reportMedia, @BatchSize IN 쿼리)
= 103번
```

#### 왜 1차 캐시로 해결되지 않나

`Step.mvpTest`처럼 같은 부모를 공유하는 경우는 1차 캐시가 중복 조회를 흡수한다. 그러나 이 경우는 다르다.

- **`report.memberTest`**: 각 Report는 서로 **다른 MemberTest**를 가리킨다. 스텝에 제출된 리포트는 멤버별로 1건이므로 MemberTest가 모두 다르다 → 매번 캐시 미스 → N번 SELECT.
- **`memberTest.member`**: 마찬가지로 MemberTest마다 다른 Member → N번 SELECT.

1차 캐시는 "같은 ID를 다시 조회할 때" 효과가 있다. 여기서는 조회 대상 ID가 모두 다르므로 캐시가 도움이 되지 않는다.

#### 해결: JOIN FETCH로 한 번에 가져오기

`ReportRepository.findAllByStepId`에 JPQL을 추가해 `report + memberTest + member`를 쿼리 1번에 조인해서 가져왔다.

```kotlin
@Query("""
    SELECT r FROM Report r
    JOIN FETCH r.memberTest mt
    JOIN FETCH mt.member
    WHERE r.step.id = :stepId
""")
fun findAllByStepId(@Param("stepId") stepId: Long): List<Report>
```

#### 왜 fetch join을 선택했나 (`reportMedia`와 다른 이유)

`@ManyToOne` JOIN FETCH는 `report 1건 = row 1건`이 유지된다. 조인 결과가 곱으로 늘어나지 않으므로 페이지네이션을 도입하더라도 `LIMIT`이 정확하게 동작한다.

`reportMedia`가 `@OneToMany`라서 JOIN FETCH 대신 `@BatchSize`를 쓴 것과 정반대의 이유다.

| 연관관계 | 해결 방법 | 이유 |
|---|---|---|
| `report.memberTest`, `memberTest.member` (@ManyToOne) | **JOIN FETCH** | row 곱 없음, 쿼리 1번으로 정확히 해소 |
| `report.reportMedia` (@OneToMany) | **@BatchSize** | JOIN FETCH 시 row 곱 발생 → 페이지네이션 불가 |

#### 최종 쿼리 횟수

| 쿼리 | 횟수 |
|---|---|
| step 단건 조회 | 1번 |
| report + memberTest + member (JOIN FETCH) | 1번 |
| reportMedia IN 쿼리 (@BatchSize) | 1번 |
| **합계** | **3번** |

---

### @ManyToOne EAGER — 단건 vs 컬렉션 조회, 그리고 1차 캐시

`Step.mvpTest`는 `@ManyToOne`이고 fetch 타입을 명시하지 않았으므로 기본값인 EAGER가 적용된다. 그런데 EAGER라고 해서 항상 추가 쿼리가 발생하는 것은 아니다.

#### 단건 조회 — JOIN으로 1번

`findByIdOrNull(stepId)`는 내부적으로 `EntityManager.find()`를 호출한다. Hibernate는 EAGER 연관관계에 대해 JOIN을 사용해 한 번에 가져온다:

```sql
SELECT s.*, mt.*
FROM step s
LEFT JOIN mvp_test mt ON s.mvp_test_id = mt.id
WHERE s.id = ?
```

쿼리 1번. 추가 쿼리 없음.

#### 컬렉션 조회 — 상황에 따라 다름

JPQL 기반 파생 쿼리(예: `findAllByMvpTestIdOrderByStepOrder`)는 `SELECT * FROM step WHERE mvp_test_id = ?`처럼 Step만 조회한다. Hibernate는 EAGER 연관관계인 `mvpTest`를 채우기 위해 각 Step에 대한 추가 SELECT를 시도하는데, 이때 **1차 캐시(영속성 컨텍스트)**가 개입한다.

- **같은 부모를 가진 컬렉션**: `findAllByMvpTestId(testId = 1)`처럼 동일한 MvpTest에 속한 Step들을 조회하면, 첫 번째 Step에서 `mvpTest(id=1)`를 DB에서 가져온 뒤 1차 캐시에 저장된다. 이후 Step들은 같은 `mvpTest`를 참조하므로 캐시 히트 — 추가 쿼리가 나가지 않는다. 결과적으로 1(Step 목록) + 1(MvpTest 최초 1회) = **2번**.

```kotlin
stepRepository.findAllByMvpTestId(testId = 1)
// step_1.mvpTest(id=1) → 캐시 미스 → SELECT mvp_test WHERE id = 1
// step_2.mvpTest(id=1) → 캐시 히트 → 쿼리 없음
// step_3.mvpTest(id=1) → 캐시 히트 → 쿼리 없음
```

- **다른 부모를 가진 컬렉션**: `findAll()`처럼 여러 MvpTest에 걸친 Step들을 조회하면, 각 Step이 가리키는 `mvpTest`가 모두 달라 매번 캐시 미스가 발생한다. Step마다 별도 SELECT → **진짜 N+1**.

```kotlin
stepRepository.findAll()
// step_1.mvpTest(id=1) → 캐시 미스 → SELECT mvp_test WHERE id = 1
// step_2.mvpTest(id=2) → 캐시 미스 → SELECT mvp_test WHERE id = 2
// step_3.mvpTest(id=3) → 캐시 미스 → SELECT mvp_test WHERE id = 3
// 총 1(Step 목록) + N(MvpTest 각각) 쿼리
```

| 상황 | 쿼리 횟수 | 이유 |
|---|---|---|
| 같은 부모 (`findAllByMvpTestId`) | 1 + 1 | 1차 캐시가 중복 조회 흡수 |
| 다른 부모 (`findAll` 등) | 1 + N | 부모가 모두 달라 매번 캐시 미스 |

#### 튜닝 기준

| 상황 | 일반적인 선택 |
|---|---|
| 단건 조회 (`findById`) | EAGER 그냥 둬도 JOIN으로 처리됨 |
| 컬렉션 조회 + 같은 부모 | 1차 캐시로 커버, 보통 그냥 둠 |
| 컬렉션 조회 + 다양한 부모 | `LAZY + JOIN FETCH` 또는 `@EntityGraph`로 명시적 처리 |

실무에서는 `@ManyToOne`을 `LAZY`로 설정하고, 연관 엔티티가 필요한 쿼리에서만 `JOIN FETCH`를 명시하는 방식을 선호한다. EAGER는 "항상 같이 쓴다"는 보장이 있을 때만 안전하다. 그 보장이 없으면 필요 없는 곳에서도 JOIN이 나가는 낭비가 생긴다.

---

## API 설계

### 리소스 계층 구조와 URL 설계

#### testId를 URL에 포함하는 이유

"특정 테스트의 스텝에 달린 리포트 목록" 조회 API를 설계할 때 두 가지 선택지가 있다:

```
A) GET /steps/{stepId}/reports          (testId 없음)
B) GET /mvp-tests/{testId}/steps/{stepId}/reports  (testId 포함)
```

B를 선택하는 이유는 두 가지다.

**1. 인가 검증이 자연스럽다.** 기업은 자신이 등록한 테스트의 리포트만 볼 수 있어야 한다. testId가 URL에 있으면 서비스 레이어에서 `mvpTest.enterpriseId == 로그인한 기업 id`를 바로 검증할 수 있다. testId가 없으면 `step → mvpTest → enterpriseId` 체인을 추적해서 검증해야 한다.

**2. 조회 맥락이 URL에 드러난다.** "이 테스트의 이 스텝에 달린 리포트"라는 의도가 URL 구조 자체에서 명확하게 표현된다.

#### 혼합 패턴 — 컬렉션과 단건 조작의 URL이 다른 경우

이 프로젝트에서는:
```
컬렉션 조회:   GET /mvp-tests/{testId}/steps/{stepId}/reports
단건 수정:     PUT /reports/{reportId}
단건 승인/거절: PUT /reports/{reportId}/approve
```

컬렉션 조회는 부모 컨텍스트(testId, stepId)가 필터 조건이므로 계층 URL을 사용한다. 단건 조작은 `reportId`가 전역적으로 유일한 자동 생성 ID이므로 부모 경로 없이도 리소스를 특정할 수 있다. 이 혼합 패턴은 GitHub, Stripe 등 대형 API에서도 쓰는 방식이다.

---

### open-in-view: false 환경에서의 트랜잭션과 LAZY 로딩

#### 세션 생명주기와 open-in-view

Spring Boot의 `open-in-view` 설정은 JPA 세션(영속성 컨텍스트)이 얼마나 오래 열려 있는지를 결정한다.

| 설정 | 세션 범위 |
|------|----------|
| `open-in-view: true` (기본값) | HTTP 요청 시작 ~ 응답 완료까지 |
| `open-in-view: false` | 트랜잭션 범위 내에서만 |

이 프로젝트는 `open-in-view: false`로 설정되어 있다. 따라서 `@Transactional`이 없는 서비스 메서드에서 Repository를 호출하면, 세션은 Repository 메서드 내부에서만 열리고 호출이 끝나는 즉시 닫힌다.

#### LazyInitializationException 발생 구조

```kotlin
// @Transactional 없음
fun getReportListByStep(...): List<ReportResponse> {
    return reportRepository.findAllByStepId(stepId)  // 세션 열림 → 쿼리 → 세션 닫힘
        .map { ReportResponse.from(it) }              // report.reportMedia 접근 → 세션 없음 → 예외
}
```

`map {}` 은 리턴 시점이 아니라 즉시 실행된다. 세션이 이미 닫힌 상태에서 LAZY 컬렉션(`reportMedia`)에 접근하므로 `LazyInitializationException`이 발생한다.

#### 해결: @Transactional(readOnly = true)

`@Transactional`을 붙이면 메서드 전체에서 하나의 세션이 유지되어 LAZY 로딩이 가능해진다. 조회 전용 메서드에는 `readOnly = true`를 함께 지정한다.

- Hibernate가 flush를 생략하고 스냅샷(dirty checking용 복사본)을 관리하지 않아 메모리·성능 이점이 있다.
- DB 드라이버나 커넥션 풀에 따라 read-only 커넥션 최적화가 적용되기도 한다.

> `open-in-view: true`는 세션을 HTTP 요청 전체에 걸쳐 열어두기 때문에 이 문제가 발생하지 않는다. 편리하지만, 뷰 레이어(컨트롤러 밖)에서도 DB 쿼리가 발생할 수 있어 성능 문제를 추적하기 어렵다는 이유로 프로덕션에서는 `false`를 권장한다.

---

### 애그리거트 루트와 @OneToMany 혼동 정정

#### 오해: 애그리거트 루트면 @OneToMany로 하위 엔티티를 들고 있어야 한다

DDD의 애그리거트 루트 개념과 JPA의 `@OneToMany`는 독립적이다.

- **DDD 애그리거트 루트**: "이 경계 안의 변경은 루트를 통해서만 해야 한다"는 불변성과 트랜잭션 경계 개념
- **JPA `@OneToMany`**: 객체 그래프를 반대 방향으로도 탐색하기 위한 기술적 도구

`Step`이 `@ManyToOne`으로 `MvpTest`를 참조하는 것만으로 소유 관계는 이미 표현된다. `MvpTest`에 `@OneToMany List<Step>`을 추가하는 건 "반대 방향으로도 JPA가 탐색할 수 있게 해달라"는 편의 요청일 뿐이다.

#### @OneToMany를 추가하는 기준

단순히 "루트니까" 또는 "조회가 편하니까"가 아니라, 아래 기준으로 판단해야 한다.

| @OneToMany를 쓰는 경우 | 피하는 경우 |
|---|---|
| Cascade 동작이 필요할 때 (루트 삭제 시 자식도 삭제) | 컬렉션 크기가 예측 불가능할 때 |
| 컬렉션 크기가 작고 항상 함께 쓸 때 (예: 주문 항목) | 페이지네이션이 필요할 때 |

`MvpTest → Step`처럼 Step이 수십~수백 개가 될 수 있는 경우, `@OneToMany`를 달면 `MvpTest`를 로딩할 때마다 Step 전체를 끌어오는 위험이 있다. 대신 `stepRepository.findAllByMvpTestId(testId)`처럼 필요할 때 직접 조회하는 방식이 더 안전하다.

---

## 페이지네이션

### 단일 id 커서의 한계 — 중복 가능한 컬럼 정렬 시 문제

`id` 단일 커서는 `ORDER BY id DESC`일 때 완벽하게 동작한다. `id`는 auto-increment이라 중복이 없고, 커서 조건 `WHERE id < cursor`가 정확히 "이전에 본 마지막 행 이후"를 의미한다.

그런데 `testStartDate` 기준 정렬처럼 **중복 가능한 컬럼**으로 정렬하면 문제가 생긴다.

```
데이터:
id=10, testStartDate=2024-06-01
id=9,  testStartDate=2024-06-01
id=8,  testStartDate=2024-06-01  ← 첫 페이지 마지막 (size=3)
id=7,  testStartDate=2024-06-01
id=6,  testStartDate=2024-05-15
```

커서를 날짜 단일 값(`cursorDate=2024-06-01`)으로 쓰면:
- `WHERE testStartDate < '2024-06-01'` → id=7이 **누락**
- `WHERE testStartDate <= '2024-06-01'` → id=10, 9, 8이 **중복 노출**

어느 쪽도 정확하지 않다.

---

### 복합 커서 설계 — (sortDate, id) 조합

`(sortDate, id)` 두 값을 커서로 사용한다. 핵심 WHERE 조건:

```sql
WHERE (sort_column < cursorDate)
   OR (sort_column = cursorDate AND id < cursorId)
ORDER BY sort_column DESC, id DESC
LIMIT size + 1
```

위 예시에서 첫 페이지 마지막 행이 `(testStartDate=2024-06-01, id=8)`이면 커서는 `cursorDate=2024-06-01, cursorId=8`.

다음 페이지 쿼리:
```sql
WHERE (testStartDate < '2024-06-01')
   OR (testStartDate = '2024-06-01' AND id < 8)
```

결과: id=7 (날짜 같고 id < 8), id=6 (날짜 작음) → 정확하게 이어진다.

**왜 이 조건이 수학적으로 정확한가**

`(date DESC, id DESC)` 정렬은 `(date, id)` 튜플의 사전식 내림차순이다. 커서 튜플 `(cursorDate, cursorId)`보다 "더 작은" 튜플을 SQL로 표현하면 정확히 위의 OR 조건이 된다:

```
(date, id) < (cursorDate, cursorId)
⟺ (date < cursorDate) OR (date = cursorDate AND id < cursorId)
```

---

### BooleanBuilder로 OR 조건 표현

QueryDSL에서 복합 커서 조건을 BooleanBuilder로 구현:

```kotlin
override fun findMvpTestListByDateCursor(
    sortBy: MvpTestSortType,
    cursorDate: LocalDateTime?,
    cursorId: Long?,
    size: Int
): List<MvpTest> {
    val sortColumn: DateTimePath<LocalDateTime> = when (sortBy) {
        MvpTestSortType.TEST_START_DATE -> mvpTest.testStartDate
        MvpTestSortType.TEST_END_DATE   -> mvpTest.testEndDate
    }
    val builder = BooleanBuilder()
    if (cursorDate != null && cursorId != null) {
        builder.and(
            sortColumn.lt(cursorDate)
                .or(sortColumn.eq(cursorDate).and(mvpTest.id.lt(cursorId)))
        )
    }
    return queryFactory.selectFrom(mvpTest)
        .where(builder)
        .orderBy(sortColumn.desc(), mvpTest.id.desc())
        .limit((size + 1).toLong())
        .fetch()
}
```

`cursorDate`와 `cursorId` 중 하나라도 null이면 첫 페이지 조회이므로 WHERE 조건 없이 전체 기준 정렬만 적용한다.

`size + 1` 트릭은 기존 단일 커서와 동일하다. 결과가 `size + 1`개이면 다음 페이지가 존재한다는 의미이며, 마지막 1개를 drop하고 그 전 행에서 다음 커서를 추출한다.

---

### MvpTestSortType enum에 QueryDSL 의존성을 두지 않는 이유

정렬 기준을 enum 메서드로 캡슐화하는 방법도 가능하다:

```kotlin
// 이렇게 하지 않는다
enum class MvpTestSortType {
    TEST_START_DATE {
        override fun sortColumn(q: QMvpTest) = q.testStartDate
    },
    TEST_END_DATE {
        override fun sortColumn(q: QMvpTest) = q.testEndDate
    };
    abstract fun sortColumn(q: QMvpTest): DateTimePath<LocalDateTime>
}
```

이 방식은 도메인 모델(`enum`)이 QueryDSL Q클래스(인프라 생성물)를 직접 참조한다. 의존성 방향이 **도메인 → 인프라**가 되어 레이어 분리 원칙을 위반한다.

대신 Repository Impl(인프라 레이어)에서 `when`으로 분기한다. enum은 도메인 의미만 갖고, 인프라 매핑은 인프라 레이어가 책임진다.

```
도메인: MvpTestSortType (TEST_START_DATE, TEST_END_DATE)
인프라: MvpTestQueryDslRepositoryImpl → when (sortBy) 로 DateTimePath 결정
```

---

### DateCursorPageResponse — 조립 로직의 위치

`CursorPageResponse`와 동일한 원칙을 따른다. `hasNext` 판단과 커서 추출은 비즈니스 로직이 아닌 페이지네이션 조립 로직이므로 DTO의 `companion object of()`가 책임진다.

```kotlin
fun <T> of(
    items: List<T>,
    size: Int,
    dateExtractor: (T) -> LocalDateTime,
    idExtractor: (T) -> Long
): DateCursorPageResponse<T>
```

Service는 `DateCursorPageResponse.of(items, size, dateExtractor) { it.id }` 한 줄로 조립한다.

### Report 목록 조회 — 기존 API 수정 vs 신규 엔드포인트

`GET /api/v1/mvp-tests/{testId}/steps/{stepId}/reports`에 페이지네이션을 추가할 때 신규 엔드포인트를 만들지 않고 기존 엔드포인트에 `cursor`, `size` 파라미터를 추가했다.

**신규 엔드포인트를 만들지 않은 이유**
- 이 API의 본질("특정 스텝의 리포트 목록 조회")은 변하지 않는다. 페이지네이션은 조회 방식의 변화이지 리소스의 변화가 아니다.
- `cursor`가 없으면 첫 페이지로 동작하므로 하위 호환이 유지된다.
- 반면 날짜 기준 정렬(`/sorted`)은 기존 id 기준 정렬과 **정렬 기준 자체가 달라** 응답 순서와 커서 타입이 다르므로 별도 엔드포인트가 적합하다.

**`@Query` JPQL 대신 QueryDSL을 선택한 이유**
`size + 1` 트릭을 쓰려면 쿼리에서 `LIMIT`을 직접 제어해야 한다. `@Query` JPQL은 `Pageable`을 통해야만 페이지 크기를 제한할 수 있는데, 이 경우 Spring의 페이지 계산 방식이 끼어들어 `size+1` 트릭을 적용할 수 없다. QueryDSL의 `.limit((size + 1).toLong())`으로 직접 제어한다.

---

## 예외 처리

### 명시되지 않은 예외가 401로 반환되는 원인

`GlobalExceptionHandler`에 등록되지 않은 예외가 발생하면 Spring이 `/error`로 포워딩한다. `/error`가 SecurityConfig의 `permitAll()` 목록에 없으면 `AuthenticationEntryPoint.commence()`가 호출되어 401이 반환된다.

```
예외 발생 (예: MissingServletRequestParameterException)
→ GlobalExceptionHandler에 핸들러 없음
→ Spring이 /error로 포워딩
→ SecurityConfig: /error 는 인증 필요
→ AuthenticationEntryPoint → 401 반환
```

파라미터 누락이 인증 오류로 보이는 혼란이 생기므로, 두 가지로 해결한다.

### GlobalExceptionHandler catch-all 패턴

```kotlin
// 클라이언트 실수 — 400으로 명시 처리
@ExceptionHandler(MissingServletRequestParameterException::class)
fun handleMissingServletRequestParameter(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse("필수 파라미터 '${e.parameterName}'이 누락되었습니다"))
}

// 서버 오류 — 500 catch-all
@ExceptionHandler(Exception::class)
fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
    logger.error("Unexpected error", e)
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse("Internal server error"))
}
```

**두 가지 원칙을 함께 지킨다:**
- 클라이언트에는 스택트레이스를 노출하지 않는다 — 공격자에게 내부 구조 정보를 주지 않기 위해
- 서버 로그에는 반드시 기록한다 — `logger.error("Unexpected error", e)`로 원인 추적 가능하게

**`Exception` catch-all이 다른 핸들러를 덮지 않는 이유**
Spring은 가장 구체적인 예외 타입의 핸들러를 우선 적용한다. `Exception`은 모든 예외의 상위 타입이므로, 더 구체적인 핸들러가 없을 때만 동작한다.

---

## 캐시

### Redis 캐시 직렬화 — JDK vs JSON

Spring Cache + Redis를 사용할 때 캐시에 저장할 객체를 바이트로 변환하는 직렬화 방식을 선택해야 한다.

#### JDK 직렬화 (기본값)

`RedisCacheConfiguration.defaultCacheConfig()`는 `JdkSerializationRedisSerializer`를 기본으로 사용한다. Java의 `ObjectOutputStream`으로 객체를 바이너리로 변환해 저장한다.

```
MvpTestResponse → ObjectOutputStream → \xAC\xED\x00\x05... → Redis
```

- 요구사항: 직렬화 대상 클래스와 모든 필드가 `Serializable` 구현 필요
- Redis에서 값 확인 불가 (바이너리)
- 클래스 구조 변경 시 `serialVersionUID` 불일치 → `InvalidClassException` 위험
- 캐싱 대상이 늘어날수록 `Serializable` 추가 대상도 증가

#### JSON 직렬화 (실무 선택)

`GenericJackson2JsonRedisSerializer`를 사용하면 JSON으로 저장된다.

```
MvpTestResponse → ObjectMapper → {"@class":"...MvpTestResponse","id":1,...} → Redis
```

- `Serializable` 불필요 → 캐싱 대상이 늘어도 DTO 변경 없음
- Redis에서 JSON으로 직접 확인 가능
- 역직렬화 시 타입 복원을 위해 `@class` 필드 포함 (`activateDefaultTyping` 설정)
- 클래스 구조 변경에 유연 (필드 추가/삭제 허용)

실무에서는 JSON 직렬화가 표준에 가깝다. JDK 직렬화는 Java/Kotlin 전용이라 다른 언어(Python 배치, Go 사이드카 등)에서 Redis 값을 읽어야 할 때 완전히 막힌다.

---

### Redis 전용 ObjectMapper를 별도로 만드는 이유

Spring Boot는 `ObjectMapper` 빈을 자동 구성해 HTTP 응답 직렬화(Controller JSON 응답), Swagger 등 전역에서 공유한다.

이 빈에 `activateDefaultTyping()`을 켜면 **모든 API 응답에 `@class` 필드가 붙어버린다.**

```json
// 클라이언트가 받는 API 응답이 이렇게 오염됨
["com.team1.mvp_test.domain.mvptest.dto.MvpTestResponse", {"id": 1, ...}]
```

`@class`는 Redis 내부 포맷에 필요한 것이지, 클라이언트에게 보내는 응답에 포함되면 안 된다.

따라서 **Redis 전용 ObjectMapper를 별도로 생성**한다. Redis에만 필요한 설정(`activateDefaultTyping`)을 공유 빈과 격리하기 위해서다.

```kotlin
val objectMapper = ObjectMapper().apply {   // Spring 빈과 무관한 새 인스턴스
    registerKotlinModule()
    registerModule(JavaTimeModule())
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL
    )
}
```

---

### KotlinModule이 필요한 이유

Jackson은 JSON을 객체로 역직렬화할 때 기본적으로 **no-arg 생성자**를 사용한다.

```java
// Java: 기본 생성자로 인스턴스 생성 후 필드 세팅
public class Foo { public Foo() {} }
```

Kotlin `data class`는 모든 필드를 받는 primary 생성자만 있고, no-arg 생성자가 없다.

```kotlin
data class MvpTestResponse(val id: Long, val mvpName: String, ...)
// MvpTestResponse() 형태의 빈 생성자 없음
```

`KotlinModule`은 Jackson에게 "Kotlin 클래스는 primary 생성자를 통해 만들어라"고 알려준다. 이게 없으면 `InvalidDefinitionException: No creators, like default constructor, exist` 예외가 발생한다.

Spring Boot의 자동 구성 `ObjectMapper`에는 `KotlinModule`이 자동 등록되어 있지만, Redis 전용으로 `ObjectMapper()`를 직접 생성하면 자동 구성과 무관한 순수 Jackson 기본 상태다. 따라서 `registerKotlinModule()`을 수동으로 등록해야 한다.

---

### @Cacheable — AOP 프록시와 내부 호출 제약

`@Cacheable`은 Spring AOP 프록시 방식으로 동작한다. 외부에서 메서드를 호출할 때 프록시가 중간에서 캐시를 확인하고 처리한다.

```
Controller → Spring 프록시 → 캐시 확인 → (미스면) 실제 getMvpTest() 호출
```

**같은 클래스 내부에서 호출하면 프록시를 거치지 않아 `@Cacheable`이 동작하지 않는다.**

```kotlin
@Service
class MvpTestService {
    @Cacheable(...)
    fun getMvpTest(testId: Long): MvpTestResponse { ... }

    fun someOtherMethod(testId: Long) {
        getMvpTest(testId)  // 내부 호출 → 프록시 미통과 → 캐시 무시 → 매번 DB 조회
    }
}
```

현재 코드에서는 `getMvpTest`가 Controller에서만 호출되므로 문제없다. 단, 나중에 같은 서비스 내부에서 `getMvpTest()`를 재사용하려 할 때 캐시가 적용되지 않아 의도와 다르게 동작한다는 점을 알고 있어야 한다.

해결이 필요해질 때의 선택지:
- 자기 자신 빈을 주입받아 프록시를 통해 호출 (코드가 지저분해짐)
- 캐시 대상 메서드를 별도 빈으로 분리 (구조가 명확해지나 클래스 증가)

---

### 캐시 적용 기준 — 어떤 API에 붙일 것인가

캐시 적용 여부를 판단하는 두 가지 기준:

1. **읽기가 쓰기보다 압도적으로 많은가** — 쓰기가 잦으면 캐시 무효화가 빈번해져 복잡도만 증가한다
2. **데이터가 약간 stale해도 괜찮은가** — 실시간 정확성이 중요한 데이터는 캐시가 맞지 않다

이 기준으로 이 프로젝트 API를 분류하면:

| API | 적합 여부 | 이유 |
|-----|----------|------|
| `GET /api/v1/mvp-tests/{testId}` | **적합** | 공개 후 반복 조회 多, 수정 少, stale 허용 |
| `GET /api/v1/mvp-tests` (목록) | **부적합** | 커서·정렬 파라미터 조합이 무한대 → 캐시 히트율 ≈ 0 |
| Report, Member, Enterprise | **불필요** | 조회 빈도가 캐시가 필요할 수준이 아님 |

목록 조회 캐시가 부적합한 이유를 더 구체적으로 설명하면, 캐시 키가 `(cursor, size, sortBy, cursorDate, cursorId)` 조합이 되어 경우의 수가 사실상 무한대다. 캐시에 저장은 되지만 동일한 키로 다시 요청이 들어올 가능성이 거의 없어 메모리만 낭비하는 구조가 된다.

---

### 응답 전체 캐싱 vs 엔티티별 분리 캐싱

`getMvpTest`는 내부적으로 DB 조회를 3번 한다 (MvpTest, Enterprise, categories). 캐싱 단위를 두 가지 방식으로 설계할 수 있다.

**방법 A: 엔티티별 분리 캐싱**
```
cache:mvptest:1     → MvpTest
cache:enterprise:1  → Enterprise
cache:categories:1  → 카테고리 목록
```
각각 별도 키로 저장하고 Service에서 조합한다. 각 데이터의 TTL과 무효화를 독립적으로 제어할 수 있다는 장점이 있다.

**단점**: Redis도 네트워크를 통한 원격 호출이다. 캐시 히트 시에도 Redis 조회가 3번 발생한다. DB IO를 줄이기 위해 Redis IO를 3배로 늘리는 아이러니가 생긴다.

**방법 B: 응답 전체 캐싱 (선택)**
```
cache:mvptest:1  → MvpTestResponse (완성된 응답 DTO 전체)
```
세 데이터가 항상 함께 사용되므로 하나의 키로 묶는다. 캐시 히트 시 DB 조회 0번, Redis 조회 1번.

**원칙**: 같은 요청에서 항상 함께 쓰이는 데이터는 하나의 키로 묶는다. 엔티티별 분리는 여러 API에서 동일 엔티티를 독립적으로 캐싱해야 할 때 의미가 있다.

---

### 연관 객체 변경 시 정합성 문제와 무효화 전략

응답 전체를 캐싱하면 연관 객체(Enterprise, categories)가 변경될 때 캐시와 DB 간 정합성이 깨질 수 있다. 세 가지 전략이 있다.

**TTL 자연 만료**
무효화 로직 없이 TTL이 지나면 자동 갱신. 구현이 단순하다.
- 적합한 경우: 연관 객체 변경이 드물고 stale 허용 가능한 데이터

**@CacheEvict — 변경 시점 즉시 무효화**
```kotlin
@CacheEvict(cacheNames = ["mvptest"], key = "#testId")
fun updateMvpTest(...) { ... }
```
변경이 일어나는 모든 지점에서 캐시를 삭제한다. 연관 객체가 많을수록 무효화해야 할 지점도 많아지고, 하나라도 누락되면 stale 데이터가 영구적으로 남을 위험이 있다.
- 적합한 경우: 정합성이 중요하고 변경 주체가 명확하고 단순할 때

**캐시 단위 축소**
정합성이 중요한 필드는 캐시에서 제외하고 매번 DB에서 조회한다. API 설계 자체가 바뀌어야 하므로 적용 난이도가 높다.
- 적합한 경우: 연관 객체 변경이 잦고 정합성이 중요하며, @CacheEvict 적용이 너무 복잡해질 때

**판단 기준 정리**

| 상황 | 전략 |
|------|------|
| 연관 객체 변경 드물고 stale 허용 | TTL 자연 만료 |
| 정합성 중요, 변경 주체 명확 | @CacheEvict |
| 정합성 중요, 변경 주체 복잡 | 캐시 단위 축소 |
| 전체가 실시간 필요 | 캐싱 포기 |

이 프로젝트의 MvpTest는 첫 번째 행에 해당한다. `enterpriseName`, `categories`는 변경이 매우 드물고 stale해도 서비스에 큰 영향이 없으므로 업데이트는 **TTL 자연 만료**를 선택한다. 단, 삭제는 별도로 판단한다.

---

### 업데이트와 삭제 — 무효화 전략이 달라야 하는 이유

"stale 허용"이라는 기준을 업데이트와 삭제에 동일하게 적용하면 안 된다. 두 상황에서 stale의 성격이 다르기 때문이다.

- **업데이트 후 stale**: "오래된 값"이 남아 있다. 클라이언트는 조금 전의 정보를 받는다. 서비스 도메인에 따라 허용 가능하다.
- **삭제 후 stale**: "없어야 할 값"이 남아 있다. 존재하지 않는 리소스가 정상 응답으로 반환된다. 클라이언트 입장에서는 결함이다.

따라서 일반적인 원칙은 이렇다:

> 삭제에서는 stale 허용 범위와 무관하게 `@CacheEvict`가 기본 선택이다.
> 중요하지 않은 데이터(예: 임시 집계값, 배너 데이터)가 아닌 이상.

이 프로젝트에서 `deleteMvpTest`에 `@CacheEvict`를 붙인 것은 이 이유다. MvpTest는 핵심 도메인이고, 삭제 후 5분간 존재하는 것처럼 응답이 나가는 것은 허용 범위를 벗어난다.

---

### @CacheEvict 사용 시 주의사항 — Cache Stampede

`@CacheEvict`는 단순히 특정 키의 캐시 엔트리를 삭제한다. 그 뒤 같은 키로 요청이 들어오면 캐시 미스가 발생하고 DB를 조회한 뒤 다시 캐싱된다.

문제는 캐시가 비워진 순간 동일 키로 **다수의 요청이 동시에** 들어올 때다.

```
@CacheEvict 실행 → 캐시 비워짐
→ 동시에 100개 요청이 같은 키 조회
→ 100개 전부 캐시 미스 → 100번 DB 조회
→ 100번 모두 캐시에 저장 (99번은 낭비)
```

이것이 **Cache Stampede** (또는 Thundering Herd) 문제다. 평소에는 캐시가 흡수하던 트래픽이 캐시가 비워진 순간 DB로 몰린다.

#### 삭제에서는 Stampede가 실제로 문제가 되기 어려운 이유

삭제 직후에는 오히려 해당 리소스로의 트래픽이 사라진다. 방금 삭제된 리소스를 100명이 동시에 조회할 시나리오가 현실적으로 성립하지 않는다. 따라서 삭제 + `@CacheEvict` 조합에서 Stampede는 이론적 가능성이지, 실질적 위험이 아니다.

#### Stampede가 실제 위험이 되는 경우 — 업데이트

트래픽이 높은 서비스에서 **인기 데이터를 업데이트**할 때 `@CacheEvict`를 사용하면 위험하다. 업데이트 후에도 조회 트래픽이 유지되기 때문에, 캐시가 비워진 순간 대기하던 수많은 요청이 DB로 몰린다.

```
인기 MvpTest(testId=1) 업데이트 → @CacheEvict
→ 1초에 수백 건이 getMvpTest(1) 호출 중
→ 모두 캐시 미스 → DB 폭주
```

이것이 업데이트에서 `@CacheEvict` 대신 TTL을 선택하는 실무적 이유 중 하나다.

#### Stampede 방지 전략 (트래픽이 매우 높을 때)

| 전략 | 방식 | 복잡도 |
|------|------|--------|
| **Mutex Lock** | 첫 번째 요청만 DB 조회, 나머지는 대기 후 캐시에서 읽기 | 중간 |
| **Probabilistic Early Expiry** | TTL 만료 전에 낮은 확률로 미리 갱신 | 중간 |
| **Background Refresh** | 별도 스레드가 TTL 만료 전 주기적으로 캐시 갱신 | 높음 |

대부분의 서비스에서는 TTL + 업데이트 후 CacheEvict 없이 자연 만료를 택하는 것만으로 충분하다. Stampede 방지 전략은 초고트래픽 서비스에서 해당 키가 실제 병목임을 측정으로 확인한 뒤 적용한다.

#### 정리

| 상황 | @CacheEvict 사용 여부 | 이유 |
|------|----------------------|------|
| 삭제 | 대부분 사용 | "없어야 할 값"이므로 즉시 제거 |
| 업데이트 + 트래픽 낮음 | 선택 가능 | Stampede 위험 낮음 |
| 업데이트 + 트래픽 높음 | 피하는 것이 안전 | Stampede 위험, TTL이 더 안전 |
| 업데이트 잦음 | CacheEvict 의미 없음 | 캐시 히트율이 0에 가까워져 캐시 효과 사라짐 |
