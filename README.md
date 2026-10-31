# deepple-api

데이팅 서비스 딥플의 백엔드 API 서버.

## 요구 사항

- JDK 21
- Docker, Docker Compose

## 시작하기

```bash
cp .env.example .env      # 값 채우기
docker compose --env-file .env up -d
```

- 앱 `http://localhost:8080`, MySQL `3308`, Redis `6381` (기본 포트, `.env`에서 변경 가능)
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- 외부 연동 값(App Store, Google Play, Firebase, Bizgo, S3)은 내부 공유 값을 받아 `.env`에 채운다.
- 키 파일은 아래 위치에 둔다. 둘 다 `.gitignore`에 포함되어 커밋되지 않는다.
  - Firebase 서비스 계정 키: `secrets/` (컨테이너의 `/etc/credentials`에 마운트)
  - Google Play 서비스 계정 키: `certs/google-play/service-account.json`

## 테스트

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
- DB 스키마는 Flyway로 관리 (`src/main/resources/db/migration`)

## 개발 플로우

트렁크 기반. `main` 하나만 사용하고 squash merge로 병합한다.

1. GitHub 이슈 생성 (이슈 타입: Feature, Bug, Task / 라벨: 도메인)
2. 브랜치 생성: `이슈번호-짧은설명` (예: `123-dating-exam-image`)
3. 커밋: `타입(범위): 설명`
   - 타입: `feat` `fix` `refactor` `test` `docs` `chore` `infra`
   - 범위: 도메인 이름, 생략 가능
   - 예: `feat(payment): 안드로이드 영수증 검증 추가`
4. PR 생성: 제목은 커밋 컨벤션 형식, 본문에 `Closes #이슈번호`
5. CI 통과 후 squash merge

Claude Code를 쓰면 `/branch <이슈번호>`, `/commit`, `/pr` 스킬로 위 과정을 처리할 수 있다.

## 배포

| 환경 | 방식 |
|---|---|
| 개발 (`dev-api.deepple.co.kr`) | `develop` push 시 자동 배포 (Blue-Green) |
| 운영 | GitHub Actions `CD - Production` 수동 실행 (ECS 롤링 업데이트) |

> 트렁크 기반 전환 중. 개발은 `main` 병합 시, 운영은 태그 push 시 배포되도록 변경 예정.

## 문서

- [Wiki](https://github.com/deepple-dev/deepple-api/wiki)
  - [시스템 아키텍처](https://github.com/deepple-dev/deepple-api/wiki/시스템-아키텍처)
  - [소프트웨어 아키텍처](https://github.com/deepple-dev/deepple-api/wiki/소프트웨어-아키텍처)
  - [CI/CD 파이프라인](https://github.com/deepple-dev/deepple-api/wiki/CI-CD-파이프라인)
