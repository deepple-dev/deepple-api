# CLAUDE.md

데이팅 서비스 딥플의 백엔드 API이다.

## 기술 스택

- Java 21, Spring Boot 4, Gradle (Version Catalog)
- JPA(Hibernate) + QueryDSL(OpenFeign fork), MySQL 8.4, Flyway
- Redis(Valkey) + Redisson
- AWS(ECS Fargate, RDS, S3), Terraform, GitHub Actions
- 테스트: JUnit 6, Mockito, Testcontainers

## 명령어

```bash
./gradlew build                                        # 빌드 + 테스트
./gradlew test                                         # 전체 테스트
./gradlew test --tests "kr.co.deepple.member.*"        # 일부 테스트
./gradlew bootRun                                      # 로컬 실행 (MySQL·Valkey·S3Mock 자동 시작)
```

## 구조

`src/main/java/kr/co/deepple/<도메인>` 단위로 나뉜다. 공통 코드는 `common`이고, 토큰 검증·보안 설정·CORS는 `common/security`에 있다.

| 도메인 | 역할 |
|---|---|
| `auth` | 회원 계정(SMS 인증, 로그인·가입), 관리자 계정(가입 신청·로그인·승인), JWT 발급, refresh token 발급·재발급·폐기 |
| `member` | 프로필·이미지 |
| `recommendation` | 이상형, 이성 소개(소개 목록, 오늘의 카드, 소울메이트·성격 유형 소개), 프로필 블러 해제 |
| `match` | 매칭 요청 메시지, 수락·거절, 메시지 내역 |
| `heart` | 하트 보유량, 하트 거래 내역 |
| `payment` | App Store·Google Play 결제, 영수증 검증, 하트 패키지 |
| `notification` | FCM 기기 등록, 알림 조회·읽음·삭제, 수신 설정 |
| `admin` | 회원 관리, 프로필 심사, 정지·경고, 신고 처리 |
| `community` | 셀프 소개, 프로필 교환 신청·수락·거절 |
| `interview` | 인터뷰 질문, 사용자 답변 |
| `like` | 좋아요 전송, 보낸·받은 목록 |
| `mission` | 일일·도전 미션, 완료 시 하트 보상 |
| `block` | 사용자 차단 |
| `report` | 사용자 신고 |
| `datingexam` | 연애고사(가치관 테스트), 결과 기반 매칭 |
| `profile` | 회원 프로필 화면 조회. 내 정보, 상대 프로필, 관리자 회원 목록·상세를 여러 모듈의 공개 포트로 모아 응답을 만든다 |

모든 도메인은 헥사고날 패키지를 쓴다.

```
event/               다른 도메인이 듣는 이벤트 (`@NamedInterface("event")`로 공개)
adapter/             in(web, event, scheduler 등 들어오는 쪽), out(persistence, 외부 연동 등 나가는 쪽)
application/         port/in(들어오는 포트), port/out(나가는 포트), dto(요청·응답·조회 조건), 서비스
domain/              엔티티, 값 객체, 도메인 안에서만 쓰는 이벤트, ErrorCode
```

`profile`은 조회 전용이라 엔티티·테이블·`adapter/out`이 없고, 다른 도메인은 `profile`을 참조하지 않는다.

## 코딩 규칙

- 컨트롤러에는 비즈니스 로직을 두지 않고 서비스에 위임한다. 성공 응답은 감싸지 않고 응답 객체를 그대로 돌려준다(목록은 `MatchViews(matches, hasMore)`처럼 리소스 복수형 필드를 가진 객체로 감싸고, 본문이 없으면 204). 오류 응답은 `GlobalExceptionHandler`가 ProblemDetail로 만든다.
- 헥사고날 모듈에서는 요청 클래스(`…Request`)를 `Command` 같은 다른 객체로 바꾸지 않고 서비스에 그대로 넘긴다(쓰는 값이 한두 개면 값만 넘겨도 된다). 요청 본문 밖의 값(토큰의 ID, 경로 값)은 메서드 인자로 따로 넘긴다. 이벤트 핸들러나 다른 모듈이 넘기는 입력을 객체로 묶을 때는 `port/in`의 `…Command`로 둔다.
- 다른 도메인의 엔티티·리포지토리를 직접 참조하지 않는다. 도메인 간 협력은 그 도메인이 공개한 포트(`application/port/in`, `@NamedInterface("port-in")`)를 부르거나(예: 회원 정보는 `FindMemberSummaryUseCase`) 그 도메인이 공개한 이벤트(`event`)를 들어서 한다. 다른 도메인이 부르는 포트의 입력·출력 객체는 `port/in`에 두고 칸은 문자열과 기본 타입으로 둔다.
  - 방향: 두 도메인 사이의 의존은 한쪽으로만 둔다(`ModularityTests`가 순환을 잡는다). 이벤트를 듣는 것도 의존이므로, 다른 도메인의 이벤트는 그 도메인이 내 포트를 부르지 않을 때만 듣는다. 내 포트를 부르는 도메인(위쪽)이 발행한 이벤트에 내가 반응해야 하면, 위쪽이 자기 `adapter/in/event`에서 자기 이벤트를 듣고 내 포트를 부른다(예: admin의 `MemberScreeningStatusEventHandler`가 `ScreeningApprovedEvent`를 듣고 member의 `ChangeMemberScreeningStatusUseCase`를 부른다)
  - 발행: 서비스가 `ApplicationEventPublisher`를 주입받아 `publishEvent(event)`로 발행한다. 엔티티는 이벤트를 만들지 않는다. 이벤트는 `Event`(`common/event`)를 구현한 `record`이고, 다른 도메인이 듣는 이벤트는 `<도메인>/event`에 둔다
  - 처리: 듣는 도메인의 `adapter/in/event`에 핸들러, 보통 `@ApplicationModuleListener`(커밋 후, 비동기, 새 트랜잭션). 외부 호출을 하거나 분산 락을 잡는 등 트랜잭션 밖에서 돌아야 하면 `propagation = Propagation.NOT_SUPPORTED`를 준다
  - 커밋 후 처리되는 이벤트는 발행할 때 `event_publication`에 기록된다(Spring Modulith Event Publication Registry). 읽기 전용 트랜잭션이나 트랜잭션 밖에서 발행하면 기록·처리되지 않으므로 쓰기 트랜잭션 안에서 발행한다
  - 기록에는 이벤트 클래스와 핸들러 메서드의 전체 이름이 저장된다. 이름이나 패키지를 바꾸기 전에 미완료 기록이 없는지 확인한다
  - 실패한 기록은 1분마다 다시 보내진다(`FailedEventResubmitter`, 시도 10번까지). 핸들러는 같은 이벤트로 여러 번 실행돼도 결과가 같게 만든다
  - 이벤트에 칸을 추가하면 이전 버전 코드는 그 기록을 읽지 못한다(`fail-on-unknown-properties`). 칸을 지우거나 이름을 바꿀 때도 미완료 기록을 먼저 확인한다
- 동시성 처리는 막을 대상에 맞게 고른다.
  - 이미 있는 행의 값 변경: 비관적 락(`@Lock(PESSIMISTIC_WRITE)`, 예: `MemberCommandRepository.findByIdForUpdate`). 락 대기는 3초(`innodb_lock_wait_timeout`)
  - 아직 없는 행의 중복 생성: unique 제약. 제약으로 표현할 수 없는 규칙이거나 저장 전 작업까지 줄 세워야 하면 `DistributedLockTemplate.executeInTransaction`(Redisson)을 함께 쓴다. 락을 잡는 메서드에는 `@Transactional`을 붙이지 않는다(락 → 트랜잭션 → 커밋 → 해제 순서).
- 엔티티는 `BaseEntity` 또는 `SoftDeleteBaseEntity`를 상속한다.
- 스키마 변경은 Flyway 마이그레이션으로만 한다. 엔티티와 마이그레이션이 일치해야 한다.
- 주석과 예외 메시지는 한국어로 쓴다.

## 개발 플로우

- 트렁크 기반으로 `main` 브랜치 하나만 쓰고 squash merge한다. `develop`은 사용하지 않는다.
- 이슈(`/issue`) → 브랜치(`/branch`) → 구현 → 테스트 → 코드 리뷰 → 커밋(`/commit`) → PR(`/pr`). 각 단계는 해당 스킬로 한다.
- 테스트: 코드를 바꾸면 관련 테스트를 추가·수정하고 `./gradlew test`로 확인한다. Docker가 켜져 있어야 한다.
- 코드 리뷰: 커밋 전에 별도 에이전트(`/code-review` 등)로 전체 변경을 리뷰하고, 지적 사항을 반영한 뒤 커밋한다.

## 하지 말 것

- `main`에 직접 커밋·push
- `.env`, `secrets/`, `certs/google-play/`의 키 파일 커밋
- 머지된 Flyway 마이그레이션 수정
- 테스트를 끄거나 지워서 빌드 통과시키기

## 세부 규칙

`.claude/rules/`에 있고, 해당 파일을 다룰 때 읽힌다.

| 파일 | 내용 | 적용 대상 |
|---|---|---|
| `java.md` | 타입·작성 규칙, Lombok | 모든 Java 파일 |
| `swagger.md` | API 문서화 | `adapter/in/web`, `application/dto` |
| `exception.md` | 예외 던지기, ErrorCode, 오류 응답 | `src/main` Java 파일 |
| `logging.md` | 로그 레벨·형식, 자동 문맥(MDC), 남기지 않는 값 | `src/main` Java 파일 |
| `resilience.md` | 외부 API 타임아웃·재시도·서킷 브레이커 | `adapter`, `common/infra`, `common/config` |
| `db.md` | 마이그레이션, 컬럼 타입 | 마이그레이션, 엔티티(`domain`) |
| `test.md` | 테스트 종류별 작성 방식 | `src/test` |
| `build.md` | 의존성 버전 관리 | `build.gradle`, `gradle/` |
| `claude-config.md` | Claude Code 설정 작성 기준 | `CLAUDE.md`, `.claude/` |
