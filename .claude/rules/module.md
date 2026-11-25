---
paths:
  - "src/main/java/**/*.java"
---

# 모듈

- 다른 도메인의 엔티티·리포지토리를 직접 참조하지 않는다. 도메인 간 협력은 그 도메인이 공개한 포트(`application/port/in`, `@NamedInterface("port-in")`)를 부르거나(예: 회원 정보는 `FindMemberSummaryUseCase`) 그 도메인이 공개한 이벤트(`event`)를 들어서 한다. 다른 도메인이 부르는 포트의 입력·출력 객체는 `port/in`에 두고 필드는 문자열과 기본 타입으로 둔다. 이벤트 핸들러가 서비스에 넘기는 입력을 객체로 묶을 때도 `port/in`의 `…Command`로 둔다.
  - 방향: 두 도메인 사이의 의존은 한쪽으로만 둔다(`ModularityTests`가 순환을 잡는다). 이벤트를 듣는 것도 의존이므로, 다른 도메인의 이벤트는 그 도메인이 내 포트를 부르지 않을 때만 듣는다. 내 포트를 부르는 도메인(위쪽)이 발행한 이벤트에 내가 반응해야 하면, 위쪽이 자기 `adapter/in/event`에서 자기 이벤트를 듣고 내 포트를 부른다(예: admin의 `MemberScreeningStatusEventHandler`가 `ScreeningApprovedEvent`를 듣고 member의 `ChangeMemberScreeningStatusUseCase`를 부른다).
  - 아래쪽 도메인이 위쪽 도메인의 값을 읽어야 하면, 아래쪽이 `application/port/out/spi`(`@NamedInterface("spi")`)에 포트를 선언하고 위쪽이 `adapter/in/spi`에서 구현한다(예: match의 `IntroductionTypeFinder`를 recommendation의 `MatchIntroductionTypeFinder`가 구현한다).
