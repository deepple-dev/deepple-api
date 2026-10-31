---
name: commit
description: 변경 사항을 커밋 컨벤션에 맞춰 커밋한다.
disable-model-invocation: true
---

1. `main` 브랜치면 커밋하지 말고 `/branch`로 브랜치를 먼저 만들라고 안내한다.
2. `git status`, `git diff --staged`로 변경을 확인한다. staged가 비어 있으면 변경 파일 목록을 보여주고 관련 파일만 stage한다. `.env`, 인증서, 비밀 값은 절대 stage하지 않는다.
3. 서로 관련 없는 변경이 섞여 있으면 커밋을 나누자고 제안한다.
4. 메시지 형식: `타입(범위): 설명`
   - 타입: `feat` 기능, `fix` 버그, `refactor` 리팩터링, `test` 테스트, `docs` 문서, `chore` 기타, `ci` CI/CD
   - 범위: 도메인 패키지 이름(`member`, `match`, `heart`, `payment` 등). 여러 도메인이거나 해당 없으면 생략.
   - 설명: 한글, 마침표 없이, 무엇을 했는지 짧게.
   - 예: `feat(payment): 안드로이드 영수증 검증 추가`
5. 커밋하고 결과 메시지를 보여준다.
