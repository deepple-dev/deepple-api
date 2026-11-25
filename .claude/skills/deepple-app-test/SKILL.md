---
name: deepple-app-test
description: 딥플 Flutter 앱(deepple_app)을 iOS 시뮬레이터에서 dev 서버(dev-api.deepple.co.kr)에 붙여 띄우고, 화면을 눌러 가며 기능을 E2E로 확인한 뒤 테스트 데이터를 정리한다. 서버 기능을 머지·배포한 뒤 "앱에서 확인해 봐", "시뮬레이터로 테스트", "dev 앱 확인", "앱이 실제로 API를 부르는지 봐 줘"처럼 앱 화면에서 동작을 확인하자는 요청이면 앱이나 시뮬레이터를 직접 말하지 않아도 사용.
---

# 딥플 앱 dev E2E 확인

서버 변경이 dev에 배포된 뒤, 실제 앱 화면에서 그 기능이 동작하는지 확인한다. 앱은 고정된 클론 하나를 계속 써서 두 번째부터는 바뀐 부분만 빌드한다(처음 몇 분, 이후 1분 안팎).

## 1. dev 서버에 변경이 들어갔는지 확인

앱만 최신이어도 서버가 옛 버전이면 확인이 무의미하다. main의 Deploy가 끝났는지 본다.

```bash
gh run list --branch main --workflow Deploy --limit 1 --json status,conclusion,headSha
```

진행 중이면 `gh run watch <id> --exit-status`를 백그라운드로 돌려 끝나길 기다린다.

## 2. 시뮬레이터를 먼저 연다

빌드 전에 시뮬레이터 도구의 `attach`부터 한다. 다른 Claude 세션이 쓰는 기기는 피한다(결과에 다른 세션이 쓰던 기기라는 안내가 나오면 다른 기기 이름을 지정해 다시 attach). `xcrun simctl list devices available`로 고를 수 있다.

## 3. 앱 클론을 dev 최신으로 맞춘다

클론 위치는 `~/Developer/deepple_app`이다. 없으면 사용자에게 위치를 묻는다. 사용자가 이 폴더에서 다른 브랜치 작업을 하기도 하니, 손대기 전에 상태를 본다.

```bash
cd ~/Developer/deepple_app && git branch --show-current && git status --short
```

- `dev`이고 깨끗하면 그대로 진행한다.
- 바뀐 파일이 빌드 부산물뿐이면 `git restore`로 되돌리고 진행한다. 지난 빌드의 pod install·코드 생성이 다시 쓴 것이라 사용자의 작업이 아니다: `ios/Podfile.lock`, `ios/Runner.xcodeproj/project.pbxproj`, `ios/Runner.xcodeproj/xcshareddata/xcschemes/*.xcscheme`, `*.g.dart`, `*.freezed.dart`.
- 다른 브랜치이거나 그 밖의 커밋하지 않은 변경이 있으면 멈추고 사용자에게 보여 준 뒤 정한다. 사용자의 작업일 수 있어서 마음대로 버리거나 브랜치를 바꾸지 않는다. 옮겨도 된다고 하면 `git stash push -m "<설명>"`으로 보관한다.

```bash
git fetch -q origin && git switch -q dev && git merge -q --ff-only origin/dev
```

deepple-dev의 앱 리포는 읽기 전용이다. 커밋·push하지 않는다.

## 4. 환경 파일

앱은 시작할 때 `assets/.env`를 읽는다(`lib/core/config/config.dart`). 없으면 흰 화면만 뜬다. 두 파일은 `.git/info/exclude`로 로컬에서만 무시해 커밋되지 않게 한다.

```bash
ls dev.env assets/.env
```

없으면 만든다.

```bash
sed 's#^BASE_URL=.*#BASE_URL=https://dev-api.deepple.co.kr#' .env.example > dev.env && cp dev.env assets/.env
for f in dev.env assets/.env; do git check-ignore -q $f || echo "/$f" >> .git/info/exclude; done
```

## 5. 빌드와 실행

길어서 백그라운드로 돌린다. pubspec이나 DTO가 바뀌지 않았으면 앞의 두 명령은 빨리 끝난다.

```bash
cd ~/Developer/deepple_app && flutter pub get && dart run build_runner build --delete-conflicting-outputs \
  && (cd ios && LANG=en_US.UTF-8 pod install) && flutter build ios --simulator --debug --flavor Debug
```

실행은 시뮬레이터 도구의 `launch`에 `app_path`로 `build/ios/iphonesimulator/Runner.app`의 절대 경로를 준다. 키체인에 남은 세션이 있으면 바로 로그인된 홈이 뜬다.

## 6. 화면 조작과 확인

**API 응답은 로그로 확인한다.** 화면만 보면 앱이 서버를 불렀는지, 응답이 무엇이었는지 알 수 없다. 앱이 요청·응답을 로그로 남긴다.

```bash
xcrun simctl spawn <UDID> log show --last 2m --predicate 'process == "Runner"' --style compact \
  | grep "(Flutter)" | grep -E "\[(REQ|RES)\]|statusCode|\"<보고 싶은 필드>\"" | cut -c60-200
```

로그인된 회원은 `[RES] GET /member/cache` 응답의 `memberId`, `nickname`으로 안다.

**한글 입력.** 시뮬레이터 키보드가 한글 자판이면 `text` 동작이 영문도 거절한다(붙여넣기 불가). 키를 하나씩 탭한다. 키보드가 뜬 상태에서 스크린샷으로 자판 위치를 확인하고 두벌식대로 누른다. 예: "테스트" = ㅌ ㅔ ㅅ ㅡ ㅌ ㅡ, "답글" = ㄷ ㅏ ㅂ ㄱ ㅡ ㄹ. 처음 키보드를 열면 "Continue" 안내가 한 번 뜬다.

**조작 요령**
- 탭은 반응이 늦게 올 수 있다. 연속으로 누르기 전에 스크린샷으로 화면이 바뀌었는지 본다.
- 홈 피드는 세로 페이지라 느린 스와이프는 넘어가지 않는다. `duration` 0.15 정도로 빠르게 스와이프한다.
- 바텀시트는 손잡이에서 아래로 스와이프해 닫는다.
- 스크린샷은 `scale` 0.3~0.4로도 충분하다. 좌표는 항상 기기 포인트(예: iPhone 18 Pro 402x874) 기준이다.

## 7. 여러 계정이 얽히는 경우는 dev API로

다른 회원·다른 등급·삭제 권한처럼 계정이 둘 이상 필요한 경우는 앱으로 하기 번거로우니 dev API를 직접 부른다. dev는 인증번호 `008008`로 아무 번호나 로그인된다.

```bash
curl -s -X POST https://dev-api.deepple.co.kr/member/login -H 'Content-Type: application/json' \
  -d '{"phoneNumber":"01077770001","code":"008008"}' | jq -r .accessToken
```

- 작성자 테스트 계정: `01077770001`~`01077770010` (GOLD 0001~0004, SILVER 0005·0009·0010, DIAMOND 0006~0008)
- 시뮬레이터 앱에 로그인된 계정과 같은 번호로 API에 로그인하면 앱 세션이 끊길 수 있다. 꼭 필요할 때(예: 앱에서 지울 수 없는 내 피드 정리)만 쓰고 그렇게 했다고 알린다. 번호는 사용자에게 묻는다.
- 토큰은 scratchpad에 파일로 두고 끝나면 지운다.
- zsh에서 `path` 같은 이름을 지역 변수로 쓰면 PATH가 덮인다. 헬퍼 함수의 변수 이름에 주의한다.

## 8. 정리

- 만든 댓글·피드는 API로 지운다. 대부분 soft delete라 행이 남는다(예: 삭제된 댓글이 목록에 "삭제된 댓글입니다"로 보이고 댓글 수에 남는다).
- 완전히 지우려면 dev DB를 고쳐야 한다. SQL을 먼저 보여 주고 확인받은 뒤 `dev-connect` 스킬 절차로 실행한다. 연 SSM 세션은 끝나면 종료한다.
- 투표·차단은 dev에서 되돌릴 수 없어(투표 변경·차단 해제 기능 없음) 꼭 필요할 때만 하고, 서버 쪽은 로컬에서 확인한다.
- 빌드 부산물(3단계 목록)을 `git restore`로 되돌려 클론을 깨끗하게 둔다. `build/`, `ios/Pods/`, `.dart_tool/`은 추적하지 않는 캐시라 남겨야 다음 빌드가 빠르다.
- 시뮬레이터를 닫는다: 시뮬레이터 도구 `detach` → `xcrun simctl shutdown <UDID>`. 사용자가 쓰지 않는다고 하면 다른 기기도 끈다.

## 결과 보고

서버 동작(응답 코드·값)과 앱 동작(화면)을 나눠서 보고한다. 응답에는 값이 있는데 화면에 안 나오면 앱 쪽 문제로 보고, 앱 리포는 고치지 않고 앱 팀에 전달할 내용으로 정리한다. 2026-11-25 기준 알려진 앱 쪽 미구현: 내가 쓴 피드 격자 칸에 글이 그려지지 않음, 격자 칸을 눌러도 상세로 가지 않음. 다음에 확인할 때 고쳐졌는지 본다.
