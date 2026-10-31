---
name: pr
description: 현재 브랜치를 push하고 main으로 PR을 만든다.
disable-model-invocation: true
---

1. `main` 브랜치면 멈추고 알린다. 커밋하지 않은 변경이 있으면 먼저 `/commit`을 하라고 안내한다.
2. 브랜치 이름(`타입/이슈번호-설명`)에서 이슈 번호를 꺼낸다.
3. `git log origin/main..HEAD`, `git diff origin/main...HEAD`로 PR 전체 변경을 파악한다.
4. PR 제목: 커밋 컨벤션 `타입(범위): 설명` 형식으로 PR 전체를 요약한다. squash merge로 이 제목이 `main`의 커밋 메시지가 된다.
5. PR 본문: `.github/pull_request_template.md` 형식을 따르고, 맨 위에 `Closes #이슈번호`를 넣는다.
6. `git push -u origin HEAD` 후 `gh pr create --base main --title "<제목>" --body "<본문>"`
7. PR 링크를 알려준다.
