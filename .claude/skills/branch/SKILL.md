---
name: branch
description: GitHub 이슈 번호로 origin/main에서 작업 브랜치를 만든다. 사용자가 브랜치 생성을 요청하거나, 이슈를 만든 뒤 구현을 시작할 때 사용.
argument-hint: <이슈 번호>
---

이슈 #$ARGUMENTS 작업 브랜치를 만든다. 이슈 번호가 비어 있으면 대화에서 찾고, 없으면 물어본다.

1. 커밋하지 않은 변경이 있으면 멈추고 알린다. 새 브랜치로 따라가 다른 작업과 섞이기 때문이다.
2. `gh issue view <이슈 번호> --json title`로 이슈를 읽는다. 이슈가 없으면 멈추고 알린다.
3. 브랜치 이름: `이슈번호-짧은설명`
   - 짧은설명: 이슈 제목을 영어 kebab-case 2~4단어로 줄인다(예: `123-dating-exam-image`).
   - `/pr`이 브랜치 이름 앞의 번호로 이슈를 찾는다.
4. `git fetch origin main && git switch -c <브랜치> origin/main`
