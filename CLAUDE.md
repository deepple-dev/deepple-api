# CLAUDE.md

데이팅 서비스 딥플의 백엔드 API. Java 21, Spring Boot 3, JPA + QueryDSL, MySQL, Redis(Redisson), Flyway.

## 명령어

```bash
./gradlew build                                        # 빌드 + 테스트
./gradlew test                                         # 전체 테스트
./gradlew test --tests "deepple.deepple.member.*"      # 일부 테스트
docker compose --env-file .env up -d                   # 로컬 실행 (app, MySQL, Redis)
```

## 구조

`src/main/java/deepple/deepple/<도메인>` 단위로 나뉜다. 공통 코드는 `common`.

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

## DB

- 스키마 변경은 Flyway 마이그레이션으로만 한다: `src/main/resources/db/migration/V{다음번호}__설명.sql`
- 이미 머지된 마이그레이션 파일은 수정하지 않는다. 새 파일을 추가한다.
- FK 제약조건은 만들지 않는다.
- PK와 `*_id` 컬럼은 `bigint`.
- dev·prod는 `ddl-auto: validate`라서 엔티티와 마이그레이션이 일치해야 한다. (local은 `update`, Flyway 꺼짐)

## 테스트

- 서비스·도메인: JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`)
- 리포지토리: `@DataJpaTest` (H2 인메모리)
- 테스트 패키지는 main과 같은 구조로 둔다.
- 코드를 바꾸면 관련 테스트를 추가·수정하고 `./gradlew test`로 확인한다.

## 개발 플로우

- 트렁크 기반: `main` 단일 브랜치, squash merge. `develop`은 사용하지 않는다.
- 이슈 → 브랜치 `이슈번호-짧은설명` → 커밋 `타입(범위): 설명` → PR(`Closes #이슈번호`)
- 타입: `feat` `fix` `refactor` `test` `docs` `chore` `infra` / 범위: 도메인 이름(생략 가능)
- 브랜치·커밋·PR은 `/branch`, `/commit`, `/pr` 스킬 규칙을 따른다.

## 하지 말 것

- `main`에 직접 커밋·push
- `.env`, `secrets/`, `certs/google-play/`의 키 파일 커밋
- 머지된 Flyway 마이그레이션 수정
- 테스트를 끄거나 지워서 빌드 통과시키기
