---
name: branch
description: GitHub 이슈 번호로 작업 브랜치를 만든다. 새 작업을 시작할 때 사용.
argument-hint: <이슈 번호>
disable-model-invocation: true
---

이슈 #$ARGUMENTS 작업 브랜치를 만든다.

1. `gh issue view $ARGUMENTS --json title`로 이슈를 읽는다. 이슈가 없으면 멈추고 알린다.
2. 브랜치 이름: `이슈번호-짧은설명`
   - 짧은설명: 이슈 제목을 영어 kebab-case 2~4단어로. 예: `123-dating-exam-image`
3. 커밋하지 않은 변경이 있으면 멈추고 알린다.
4. `git fetch origin main && git switch -c <브랜치> origin/main`
