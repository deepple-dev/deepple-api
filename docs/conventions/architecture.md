# 아키텍처 규칙

DDD + 코드 레벨 CQRS. 도메인(바운디드 컨텍스트) 단위로 패키지를 나누고, 각 도메인 안에서 command/query를 분리한다.

## 1. 패키지 구조

```
{도메인}/
├── presentation/            # 컨트롤러, 요청/응답 DTO, 도메인 예외 핸들러
│   └── dto/
├── command/
│   ├── application/         # 애플리케이션 서비스, Mapper, 유스케이스 예외
│   ├── domain/              # 엔티티, VO, 도메인 이벤트, 리포지토리 인터페이스, 도메인 예외
│   │   ├── event/
│   │   └── exception/
│   └── infra/               # JPA 리포지토리, 외부 API 클라이언트, 이벤트 핸들러
└── query/
    └── {하위도메인}/
        ├── application/     # QueryService, SearchCondition, Fetcher
        ├── infra/           # QueryDSL QueryRepository
        └── view/            # 조회 결과 record
```

## 2. 레이어 의존 규칙

- 의존 방향: `presentation → application → domain ← infra`
- domain은 어떤 레이어에도 의존하지 않는다 (Spring 어노테이션 중 JPA·이벤트 관련만 허용).
- presentation은 domain 엔티티를 직접 반환하지 않는다 — 항상 DTO/View로 변환 후 반환.
- infra는 domain의 인터페이스를 구현한다 (예: `MemberCommandRepository` ← `MemberCommandRepositoryImpl`).

## 3. 도메인 간 참조 규칙

- **엔티티 연관관계는 도메인 경계를 넘지 않는다.** 다른 도메인의 엔티티는 ID(Long)로만 참조한다.
  (예: `Match.requesterId`, `Like.senderId` — `@ManyToOne Member` 금지)
- 도메인 간 상태 변경 전파는 **도메인 이벤트**로 한다. 다른 도메인의 서비스/리포지토리 직접 호출은 지양한다.
- query 측에서 여러 도메인을 QueryDSL JOIN으로 묶는 것은 허용한다 (조회 전용이므로). 단, command 측의 크로스 도메인 import는 부채로 간주한다.
- 현재 크로스 도메인 직접 import가 약 40건 존재 — 신규 코드에서 추가 금지, 해소는 ROADMAP 17~18번(Spring Modulith)에서 진행.

## 4. 도메인 이벤트

- 발행: 도메인 엔티티 또는 서비스에서 `Events.raise({동작}Event.of(...))` — **반드시 트랜잭션 내부에서** 호출한다. 트랜잭션 밖에서 raise하면
  `@TransactionalEventListener` 핸들러가 조용히 실행되지 않는다 (ROADMAP D6).
- 이벤트 클래스: `{동작완료형}Event` (예: `ScreeningApprovedEvent`), 위치는 `command/domain/.../event/`, `Event` 상속 + private 생성자 + 정적
  팩토리.
- 핸들러: `{도메인}EventHandler`, 위치는 수신 도메인의 `command/infra/`, 메서드명은 `handle(이벤트)`.
- 핸들러 어노테이션 표준: `@Async` + `@TransactionalEventListener(phase = AFTER_COMMIT)`.
    - 같은 트랜잭션에서 처리해야 하는 경우(정합성 필수)에만 동기 `@EventListener` 사용을 검토하고, 사유를 주석으로 남긴다.
- 핸들러에서 예외를 빈 catch로 삼키지 않는다. 실패는 로그 + 재시도/기록 대상 (ROADMAP 19~20번에서 아웃박스·재시도 정책으로 체계화 예정).

## 5. 트랜잭션 경계

- `@Transactional`은 **application 서비스**에 선언한다. domain과 presentation에는 붙이지 않는다.
- 커맨드 서비스: 메서드 레벨 `@Transactional`
- 쿼리 서비스: 클래스 레벨 `@Transactional(readOnly = true)`
- `REQUIRES_NEW` 등 전파 속성 변경은 사유를 주석으로 남긴다.

## 6. 영속성

- 모든 엔티티는 `BaseEntity`(createdAt/updatedAt) 또는 `SoftDeleteBaseEntity`(+ deletedAt) 상속.
- ID 전략: `@GeneratedValue(strategy = IDENTITY)`
- enum 컬럼: `@Enumerated(EnumType.STRING)` + `@Column(columnDefinition = "varchar(50)")`
- 의미 있는 값은 `@Embedded` VO로 캡슐화 (PhoneNumber, HeartBalance 등). 컬럼명 충돌 시 `@AttributeOverride`.
- 리포지토리 구조:
    - 표준: domain 인터페이스 + infra 구현(Impl) + infra JPA 인터페이스 3단 분리
    - 단순 CRUD만 필요한 경우 domain에서 `JpaRepository` 직접 상속 허용
- 인덱스는 엔티티 `@Table(indexes = ...)`에 선언하고 Flyway 마이그레이션으로 반영한다.

## 7. Flyway 마이그레이션

- 파일명: `V{번호}__{설명}.sql` (snake_case 영어 설명)
- 하나의 마이그레이션은 하나의 목적만 담는다 (테이블 생성 / 인덱스 추가 / 데이터 삽입 분리).
- dev/prod에서 `ddl-auto: validate` + Flyway 활성화. 스키마 변경은 반드시 마이그레이션으로만 한다.
- 무중단 배포 환경이므로 파괴적 변경(컬럼 drop·rename)은 expand-contract 절차를 따른다 (ROADMAP 36번에서 상세 규칙 문서화 예정).

## 8. 설정

- 환경값은 yml에서 `${ENV_VAR:기본값}` 형식으로 주입. 시크릿을 yml에 하드코딩하지 않는다.
- 프로파일: `local`(ddl-auto: update, Flyway off) / `dev` / `prod`(validate, Flyway on)
- 공통 인프라 설정은 `common/config/`, 도메인 특화 설정(외부 API 클라이언트 등)은 해당 도메인의 `infra/`에 둔다.
