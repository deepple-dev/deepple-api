---
name: issue
description: 작업 설명으로 GitHub 이슈를 만든다. 새 작업을 이슈로 등록할 때 사용.
argument-hint: <작업 설명>
disable-model-invocation: true
---

"$ARGUMENTS" 작업으로 이슈를 만든다.

1. 이슈 타입을 정한다: 새 기능 `Feature`, 버그 `Bug`, 그 외(리팩터링·테스트·문서·CI·인프라) `Task`.
2. 제목: 한글, 마침표 없이, 무엇을 하는지 짧게. 타입 접두어(`feat:` 등)는 붙이지 않는다.
3. 본문: `.github/ISSUE_TEMPLATE/`의 해당 템플릿(`feature.md`, `bug.md`, `task.md`) 섹션을 따른다.
   - 불릿 포인트로 핵심만 간결하게. 문장은 명사형 어미로 끝낸다. 예: `- 안드로이드 영수증 검증 API 추가`
   - 버그는 재현 방법·예상 동작·실제 동작을 채운다. 모르는 내용은 지어내지 말고 물어본다.
4. 라벨: `gh label list`에서 관련 도메인 라벨(`member`, `payment`, `infra` 등)을 고른다. 맞는 라벨이 없으면 생략.
5. 본문을 파일로 쓰고 생성한다. `gh issue create`는 타입을 지정할 수 없으므로 `gh api`를 쓴다.
   ```
   gh api repos/:owner/:repo/issues -f title="<제목>" -F body=@<본문 파일> -f type=<타입> \
     -f 'labels[]=<라벨>' -f "assignees[]=$(gh api user -q .login)" -q '.html_url'
   ```
6. 이슈 링크를 알려주고, 바로 작업하려면 `/branch <이슈 번호>`를 쓰라고 안내한다.
