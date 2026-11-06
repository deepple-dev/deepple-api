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
./gradlew test --tests "deepple.deepple.member.*"      # 일부 테스트
./gradlew bootRun                                      # 로컬 실행 (MySQL·Valkey·S3Mock 자동 시작)
```

## 구조

`src/main/java/deepple/deepple/<도메인>` 단위로 나뉜다. 공통 코드는 `common`.

| 도메인 | 역할 |
|---|---|
| `auth` | JWT 발급·검증·재발급, 인증 필터, CORS |
| `member` | SMS 인증, 프로필·이미지, 이상형, 이성 소개, 프로필 블러 해제 |
| `match` | 매칭 요청 메시지, 수락·거절, 메시지 내역 |
| `heart` | 하트 보유량, 하트 거래 내역 |
| `payment` | App Store·Google Play 결제, 영수증 검증, 하트 패키지 |
| `notification` | FCM 기기 등록, 알림 조회·읽음·삭제, 수신 설정 |
| `admin` | 관리자 인증, 회원 관리, 프로필 심사, 정지·경고, 신고 처리 |
| `community` | 셀프 소개, 프로필 교환 신청·수락·거절 |
| `interview` | 인터뷰 질문, 사용자 답변 |
| `like` | 좋아요 전송, 보낸·받은 목록 |
| `mission` | 일일·도전 미션, 완료 시 하트 보상 |
| `block` | 사용자 차단 |
| `report` | 사용자 신고 |
| `datingexam` | 연애고사(가치관 테스트), 결과 기반 매칭 |

도메인마다 두 가지 스타일이 있다. 새 코드는 해당 도메인의 기존 스타일을 따른다.

- CQRS 스타일 (대부분의 도메인)
  ```
  presentation/        컨트롤러, 요청·응답 DTO
  command/application  쓰기 서비스
  command/domain       엔티티, 값 객체, 도메인 이벤트, 리포지토리 인터페이스, ErrorCode
  command/infra        리포지토리 구현, 외부 연동, 이벤트 핸들러
  query/               읽기 전용 조회 (QueryDSL)
  ```
- 헥사고날 스타일 (`block`, `datingexam`)
  ```
  adapter/             webapi, database, listener 등 입출력
  application/         provided(제공 포트), required(필요 포트), 서비스
  domain/              엔티티, 값 객체, ErrorCode
  ```

## 코딩 규칙

- 컨트롤러에는 비즈니스 로직을 두지 않고 서비스에 위임한다. 성공 응답은 `BaseResponse`로 감싸고, 오류 응답은 `GlobalExceptionHandler`가 ProblemDetail로 만든다.
- 다른 도메인의 엔티티·리포지토리를 직접 참조하지 않는다. 도메인 간 협력은 도메인 이벤트로 한다.
  - 발행: `Events.raise(event)` (`common/event`), 이벤트는 `Event`를 상속
  - 처리: 다른 도메인의 `infra`에 핸들러, 보통 `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`
- 동시성이 필요한 처리는 `LockRepository`(Redisson 분산락)를 쓴다.
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
| `java.md` | 타입·이름·작성 규칙, Lombok | 모든 Java 파일 |
| `swagger.md` | API 문서화 | `presentation`, `adapter/webapi`, `query` |
| `resilience.md` | 외부 API 타임아웃·재시도·서킷 브레이커 | `infra`, `adapter`, `common/config` |
| `db.md` | 마이그레이션, 컬럼 타입 | 마이그레이션, 엔티티(`domain`) |
| `test.md` | 테스트 종류별 작성 방식 | `src/test` |
| `build.md` | 의존성 버전 관리 | `build.gradle`, `gradle/` |
| `claude-config.md` | Claude Code 설정 작성 기준 | `CLAUDE.md`, `.claude/` |
