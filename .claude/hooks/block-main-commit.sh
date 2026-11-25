#!/bin/bash
input=$(cat)
cmd=$(printf '%s' "$input" | jq -r '.tool_input.command // empty' 2>/dev/null)
dir=$(printf '%s' "$input" | jq -r '.cwd // empty' 2>/dev/null)
[ -n "$dir" ] || dir="$CLAUDE_PROJECT_DIR"
git_prefix='(^|[;&|[:space:]])git([[:space:]]+-[Cc][[:space:]]+[^[:space:]]+)*[[:space:]]+'
printf '%s' "$cmd" | grep -qE "${git_prefix}(commit|push)([[:space:]]|$)" || exit 0
cd_target=$(printf '%s' "$cmd" | sed -nE 's/^[[:space:]]*cd[[:space:]]+([^[:space:];&|]+).*/\1/p')
c_target=$(printf '%s' "$cmd" | sed -nE 's/.*git[[:space:]]+-C[[:space:]]+([^[:space:]]+).*/\1/p')
for target in "$cd_target" "$c_target"; do
  [ -n "$target" ] || continue
  target="${target%\"}"; target="${target#\"}"; target="${target%\'}"; target="${target#\'}"
  case "$target" in
    /*) dir="$target" ;;
    "~"*) dir="$HOME${target#\~}" ;;
    *) dir="$dir/$target" ;;
  esac
done
branch=$(git -C "$dir" branch --show-current 2>/dev/null)
if [ "$branch" = "main" ] || printf '%s' "$cmd" | grep -qE "${git_prefix}push.*([[:space:]]|:|/|\+)main([[:space:]]|$)"; then
  echo "main 브랜치에 직접 커밋·push하지 않는다. /branch로 작업 브랜치를 만든 뒤 진행한다." >&2
  exit 2
fi
exit 0
