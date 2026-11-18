---
paths:
  - "src/main/java/**/adapter/in/web/**/*.java"
  - "src/main/java/**/application/**/*.java"
---

# Web

- 컨트롤러에는 비즈니스 로직을 두지 않고 서비스에 위임한다.
- 성공 응답은 감싸지 않고 응답 객체를 그대로 돌려준다. 목록은 `MatchViews(matches, hasMore)`처럼 리소스 복수형 필드를 가진 객체로 감싸고, 본문이 없으면 204로 응답한다.
- 요청 클래스(`…Request`)를 `Command` 같은 다른 객체로 바꾸지 않고 서비스에 그대로 넘긴다. 쓰는 값이 한두 개면 값만 넘겨도 된다.
- 요청 본문 밖의 값(토큰의 ID, 경로 값)은 메서드 인자로 따로 넘긴다.
