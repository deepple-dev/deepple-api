---
name: branch
description: GitHub 이슈 번호로 작업 브랜치를 만든다. 새 작업을 시작할 때 사용.
argument-hint: <이슈 번호>
disable-model-invocation: true
---

이슈 #$ARGUMENTS 작업 브랜치를 만든다.

1. `gh issue view $ARGUMENTS --json title,body`와 `gh api repos/:owner/:repo/issues/$ARGUMENTS -q .type.name`(이슈 타입)으로 이슈를 읽는다. 이슈가 없으면 멈추고 알린다.
2. 브랜치 이름: `타입/이슈번호-짧은설명`
   - 타입: `feat` `fix` `refactor` `test` `docs` `chore` `infra` 중 하나. 이슈 타입 Feature → `feat`, Bug → `fix`, Task → 제목·내용을 보고 `refactor` `test` `docs` `chore` `infra` 중 선택. 애매하면 물어본다.
   - 짧은설명: 이슈 제목을 영어 kebab-case 2~4단어로. 예: `feat/123-dating-exam-image`
3. 커밋하지 않은 변경이 있으면 멈추고 알린다.
4. `git fetch origin main && git switch -c <브랜치> origin/main`
