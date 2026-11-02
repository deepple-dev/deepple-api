# CLAUDE.md

데이팅 서비스 딥플의 백엔드 API. Java 21, Spring Boot 3, JPA + QueryDSL, MySQL, Redis(Redisson), Flyway.

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
  presentation/        컨트롤러, 요청·응답 DTO, 도메인별 ExceptionHandler
  command/application  쓰기 서비스
  command/domain       엔티티, 값 객체, 도메인 이벤트, 리포지토리 인터페이스
  command/infra        리포지토리 구현, 외부 연동, 이벤트 핸들러
  query/               읽기 전용 조회 (QueryDSL)
  ```
- 헥사고날 스타일 (`block`, `datingexam`)
  ```
  adapter/             webapi, database, listener 등 입출력
  application/         provided(제공 포트), required(필요 포트), 서비스
  domain/              엔티티, 값 객체
  ```

## 코딩 규칙

- 컨트롤러에는 비즈니스 로직을 두지 않고 서비스에 위임한다. 응답은 `BaseResponse`로 감싼다.
- 다른 도메인의 엔티티·리포지토리를 직접 참조하지 않는다. 도메인 간 협력은 도메인 이벤트로 한다.
  - 발행: `Events.raise(event)` (`common/event`), 이벤트는 `Event`를 상속
  - 처리: 다른 도메인의 `infra`에 핸들러, 보통 `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`
- 동시성이 필요한 처리는 `LockRepository`(Redisson 분산락)를 쓴다.
- 엔티티는 `BaseEntity` 또는 `SoftDeleteBaseEntity`를 상속한다.
- 주석과 예외 메시지는 한국어.
- 세부 규칙은 `.claude/rules/`: Java(`java.md`), Swagger 문서화(`swagger.md`), 외부 API 연동(`resilience.md`)

## DB

- 스키마 변경은 Flyway 마이그레이션으로만 한다: `src/main/resources/db/migration/V{다음번호}__설명.sql`
- 이미 머지된 마이그레이션 파일은 수정하지 않는다. 새 파일을 추가한다.
- FK 제약조건은 만들지 않는다.
- PK와 `*_id` 컬럼은 `bigint`.
- local·dev·prod 모두 Flyway + `ddl-auto: validate`라서 엔티티와 마이그레이션이 일치해야 한다. 로컬 실행에서 바로 확인된다.

## 테스트

- 서비스·도메인: JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`)
- 리포지토리: `@DataJpaTest` (H2 인메모리)
- 테스트 패키지는 main과 같은 구조로 둔다.
- 코드를 바꾸면 관련 테스트를 추가·수정하고 `./gradlew test`로 확인한다.

## 개발 플로우

- 트렁크 기반: `main` 단일 브랜치, squash merge. `develop`은 사용하지 않는다.
- 이슈(`/issue`) → 브랜치(`/branch`) → 구현 → 코드 리뷰 → 커밋(`/commit`) → PR(`/pr`). 각 단계는 해당 스킬로 한다.
- 코드 리뷰: 커밋 전에 별도 에이전트(`/code-review` 등)로 전체 변경을 리뷰하고, 지적 사항을 반영한 뒤 커밋한다.
- 타입: `feat` `fix` `refactor` `test` `docs` `chore` `infra` / 범위: 도메인 이름(생략 가능)

## 하지 말 것

- `main`에 직접 커밋·push
- `.env`, `secrets/`, `certs/google-play/`의 키 파일 커밋
- 머지된 Flyway 마이그레이션 수정
- 테스트를 끄거나 지워서 빌드 통과시키기
