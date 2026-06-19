# Git 컨벤션

## 1. 브랜치 전략 (간소화 Git Flow — 2026-06-11 확정)

```
feat/*  ──PR──▶  develop  ──PR──▶  main
                 (dev 서버          (prod 수동 배포,
                  자동 배포)         머지마다 태그)
hotfix/* : main에서 분기 → main 머지 + 패치 태그 → develop back-merge
```

- **main** — 프로덕션 상태. 직접 커밋 금지, develop 또는 hotfix/*의 PR로만 변경
- **develop** — 통합 브랜치. 머지 시 dev 서버(dev-api.deepple.co.kr) 자동 배포. 직접 커밋 금지
- **release/* 브랜치는 사용하지 않는다** — dev 서버가 QA 환경 역할. QA 기간 동안 develop을 얼릴 필요가 생기면 그때 도입 검토

### 규칙 5가지

1. **feat/* → develop은 항상 PR 경유.** 로컬 머지 후 push 금지. 셀프 머지라도 PR을 거쳐 CI 통과 기록과 변경 목록을 남긴다.
2. **develop → main 머지는 작고 자주.** 기능 1~3개 단위. 거대 배치 릴리스 금지.
3. **main 머지마다 SemVer 태그** (`v1.4.0`). prod에 무엇이 떠 있는지는 항상 마지막 태그로 답할 수 있어야 한다.
4. **hotfix/*는 main에서 분기.** 수정 → main 머지 + 패치 태그(`v1.4.1`) → develop에 back-merge.
5. **main·develop 브랜치 보호.** PR 필수 + CI 통과 필수 (GitHub Settings → Branches).

### 브랜치 네이밍

- `feat/{이슈번호 또는 설명}` — 기능 (예: `feat/444`, `feat/dating-exam-personality-type`)
- `fix/{설명}` — develop 대상 버그 수정
- `hotfix/{설명}` — main 대상 긴급 수정
- `docs/{설명}`, `chore/{설명}`, `refactor/{설명}` — 비기능 변경

## 2. 커밋 컨벤션

형식: `{type}: {한국어 제목}` 또는 `{type}({scope}): {한국어 제목}`

```
feat: 관리자 하트 지급 기능 추가
feat(community): 셀프 소개 이미지 업로드 기능 추가
fix: 결제 영수증 검증 실패 시 응답 코드 수정
docs: plan 문서 추가
chore: Google Play 결제 디버깅 로그 제거
refactor: 하트 차감 로직 분리
test: 매칭 동시 요청 테스트 추가
```

- type: `feat` `fix` `docs` `chore` `refactor` `test` `perf` `ci`
- 제목은 한국어, 명사형 종결 ("~추가", "~수정", "~제거"), 마침표 없음
- 하나의 커밋은 하나의 논리적 변경만 담는다

## 3. PR 규칙

- 제목은 커밋 컨벤션과 동일한 형식
- 본문에 포함: 변경 이유(왜), 주요 변경 내용, 테스트 방법 또는 확인 사항
- develop → main PR 제목은 릴리스 내용을 요약 (단순히 "Develop" 금지 — 포함된 기능을 나열)
- PR은 작게: 리뷰 가능한 단위(±500라인 내외)를 지향
- CI 통과 전 머지 금지

## 4. 태그·릴리스

- 형식: `v{major}.{minor}.{patch}` (SemVer)
    - major: 호환성이 깨지는 변경 (API 계약 변경 등)
    - minor: 기능 추가
    - patch: 버그 수정·핫픽스
- main 머지 직후 머지 커밋에 태그를 찍는다
- 릴리스 노트 자동 생성은 ROADMAP 5번(release-drafter)에서 도입
