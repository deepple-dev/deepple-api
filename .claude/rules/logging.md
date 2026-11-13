---
paths:
  - "src/main/java/**/*.java"
---

# 로깅

로그는 CloudWatch(ECS JSON), Grafana(OTEL), Sentry(ERROR 이상)로 나간다.

## 레벨

- ERROR: 사람이 조치해야 하는 일. Sentry 이슈가 되어 알림이 온다(예: 결제 소비 실패, 예상하지 못한 예외).
- WARN: 이상하지만 시스템이 처리하고 넘어간 일, 보안상 지켜볼 일(예: 푸시 전송 실패, 리프레시 토큰 재사용).
- INFO: 나중에 찾아볼 비즈니스·감사 기록(예: 결제·환불 완료, 관리자 하트 지급). 요청마다 반복되는 정상 흐름은 남기지 않는다.
- DEBUG는 쓰지 않는다.

## 형식

- 값은 SLF4J key-value API로 넣고, 메시지는 `{}` 없는 한국어 고정 문장으로 쓴다.
  ```java
  log.atWarn()
      .addKeyValue("receiverId", receiverId)
      .addKeyValue("errorCode", errorCode)
      .log("FCM 전송 실패");
  ```
- 예외는 `setCause(e)`로 넘긴다.
- key는 영어 camelCase로 쓴다. 표준 필드(`url.path` 등)는 OpenTelemetry·ECS 이름을 쓴다.
- `error.*`는 ECS가 예외 정보를 쓰는 자리라 key로 쓰지 않는다(예: `errorCode`). 이름이 겹치면 그 로그는 출력되지 않는다.

## 자동 문맥

- `user.id`, `user.roles`, `http.request.method`, `http.route`, `event.action`은 MDC로 자동으로 붙는다(`common/logging`). 메시지나 key-value에 다시 넣지 않는다. MDC와 같은 key를 key-value로 넣으면 그 로그는 출력되지 않는다.
  - 요청: `RequestLogContextInterceptor`
  - 이벤트 핸들러·스케줄러: `EventLogContextAspect`
  - `@Async`: `AsyncConfig`가 MDC를 다른 스레드로 전달
- 요청한 회원과 다른 회원의 ID(`receiverId`, `blockedId` 등)만 key-value로 넣는다.

## log and throw 금지

- 예외를 던지면 로그를 남기지 않는다. `GlobalExceptionHandler`가 한 번 남긴다. 추적에 필요한 값은 예외 detail에 넣는다.
- 예외를 처리하고 끝낼 때만 로그를 남긴다.
- 던지는 오류와 다른 사실(예: 리프레시 토큰 재사용 감지)은 따로 남겨도 된다.

## 이벤트 핸들러

- `@Async` 이벤트 핸들러에서 `catch (Exception)`으로 로그만 남기지 않는다. 처리하지 못한 예외는 `AsyncConfig`의 `AsyncUncaughtExceptionHandler`가 ERROR로 남긴다.
- 비동기 실패 로그에는 이벤트 종류(`event.action`)와 이벤트 ID(`eventId`)가 자동으로 붙는다. 이벤트에 로그용 값을 따로 담지 않는다.
- 이미 있음처럼 예상된 실패는 예외 대신 멱등 메서드(`createIfAbsent` 등)로 처리한다.

## 남기지 않는 값

- 개인정보: 전화번호, 이름·닉네임, 인증번호
- 토큰·secret: access·refresh·FCM 토큰, purchaseToken, 비밀번호, API 키
- 결제 원문: 영수증, Apple `signedPayload`
- 외부 API 응답 원문(헤더·본문). 필요한 값(오류 코드 등)만 골라 남긴다.
- 자유 입력 원문: 자기소개, 메시지 내용
- 로컬에서만 켜지는 Fake 구현(`Fake*`)은 테스트를 위해 예외로 둔다.
