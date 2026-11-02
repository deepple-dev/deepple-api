---
paths:
  - "CLAUDE.md"
  - ".claude/**"
---

# Claude Code 설정

스킬, CLAUDE.md, rules, 에이전트 같은 Claude Code 설정은 Anthropic이 제공하는 가이드를 기준으로 만들고 고친다. 기준이 애매하면 `claude-code-guide` 에이전트로 공식 문서를 확인한다.

- 스킬(`.claude/skills/`): `skill-creator` 스킬을 따른다. `description`에는 무엇을 하는지와 언제 쓰는지를 쓰고, 본문은 간결하게 둔다([Skills](https://code.claude.com/docs/en/skills)).
- CLAUDE.md: 모든 세션에 필요한 사실만 200줄 이하로 쓴다. 지시는 검증할 수 있게 구체적으로 쓴다([Memory](https://code.claude.com/docs/en/memory)).
- rules(`.claude/rules/`): 파일 하나에 주제 하나를 둔다. 특정 파일에만 필요한 규칙은 `paths`로 범위를 정하고, CLAUDE.md의 세부 규칙 표에 추가한다.
- 에이전트(`.claude/agents/`): `name`, `description`을 쓰고 필요한 도구만 준다([Subagents](https://code.claude.com/docs/en/sub-agents)).
