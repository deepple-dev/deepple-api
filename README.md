# deepple-api

데이팅 서비스 딥플의 백엔드 API 서버이다.

## 요구 사항

- JDK 21
- Docker (Docker Desktop 등)

## 시작하기

Docker를 켠 상태에서 앱을 실행한다. 설정 파일이나 키 파일은 필요 없다.

```bash
./gradlew bootRun    # 또는 IntelliJ에서 DeeppleApplication 실행
```

- 기본 프로필은 `local`이다. 앱이 `compose.yml`의 MySQL, Valkey, S3Mock을 자동으로 띄우고, 앱을 꺼도 컨테이너는 남는다.
- 앱 `http://localhost:8080`, Swagger UI `http://localhost:8080/swagger-ui.html`
- 포트: MySQL `3306`(DB·계정·비밀번호 `deepple`), Valkey `6379`, S3Mock `9090`(버킷 `deepple-local`)
- 스키마는 Flyway 마이그레이션으로 만들어진다. 데이터를 지우려면 `docker compose down -v`를 실행한다.
- 로컬에서는 외부 연동을 가짜 구현으로 대신한다(`application-local.yml`의 `bizgo.enabled`, `firebase.enabled`, `payment.google-play.enabled`가 `false`).
  - 문자 인증번호: 실제로 발송하지 않고 로그에 `[문자 발송]`으로 출력, 고정 코드 `008008`로도 로그인 가능
  - 푸시(FCM), Google Play 결제 검증: 요청하지 않고 로그만 남김, Google Play 구매는 항상 유효로 처리
  - 이미지 업로드: S3Mock에 저장

### IntelliJ

- `Settings > Editor > Code Style > Scheme`을 `Project`로 설정 (저장소의 `.idea/codeStyles` 사용)
- `Settings > Tools > Actions on Save`에서 `Reformat code`, `Optimize imports`, `Rearrange code` 켜기
- (선택) SonarQube for IDE 플러그인 설치

## 테스트

Docker를 켠 상태에서 실행한다. 테스트가 MySQL·Valkey·S3Mock 컨테이너를 직접 띄운다.

```bash
./gradlew test
```

## 프로젝트 구조

도메인별 패키지(`src/main/java/deepple/deepple/<도메인>`)로 나뉘고, 도메인 간 협력은 도메인 이벤트로 처리한다.

```
<도메인>/
├── presentation/   # API 컨트롤러, 요청·응답 DTO
├── command/        # 쓰기: application, domain, infra
└── query/          # 읽기 전용 조회
```

- 도메인: `member`, `match`, `heart`, `payment`, `notification`, `admin`, `community`, `interview`, `like`, `mission`, `block`, `report`, `datingexam`, `auth`
- DB 스키마: Flyway(`src/main/resources/db/migration`)

## 개발 플로우

트렁크 기반으로 `main` 하나만 사용하고 squash merge로 병합한다.

1. GitHub 이슈 생성 (이슈 타입: Feature, Bug, Task / 라벨: 도메인)
2. 브랜치 생성: `이슈번호-짧은설명` (예: `123-dating-exam-image`)
3. 커밋: `타입(범위): 설명`
   - 타입: `feat` `fix` `refactor` `test` `docs` `chore` `infra`
   - 범위: 도메인 이름, 생략 가능
   - 예: `feat(payment): 안드로이드 영수증 검증 추가`
4. PR 생성: 제목은 커밋 컨벤션 형식, 본문에 `Closes #이슈번호`
5. CI 통과 후 머지 큐로 squash merge (최신 `main`과 합친 상태로 CI를 한 번 더 돌린 뒤 반영)

Claude Code를 쓰면 `/issue <작업 설명>`, `/branch <이슈번호>`, `/commit`, `/pr` 스킬로 위 과정을 처리할 수 있다.

## 배포

| 환경 | 방식 |
|---|---|
| 개발 | `main` 병합 시 앱 코드가 바뀌었으면 자동 배포, Actions의 Deploy에서 수동 실행 가능 (`dev-api.deepple.co.kr`) |
| 운영 | 없음 (Terraform으로 재구축 중) |

- 개발 배포: 이미지 빌드 → ECR push(커밋 SHA 태그) → 출처 증명·SBOM 서명 → ECS 서비스 갱신 → 완료 알림 `#alerts-dev`
- 이미지 검증: `gh attestation verify oci://<ECR 주소>/deepple-api@<digest> --repo deepple-dev/deepple-api`

## 문서

- [CLAUDE.md](CLAUDE.md): 구조, 도메인, 코딩 규칙
- [`.claude/rules/`](.claude/rules): Java, Swagger, 외부 API 연동, DB, 테스트, 빌드 규칙
