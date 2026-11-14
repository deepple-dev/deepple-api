---
paths:
  - "src/main/java/**/*.java"
---

# 예외 처리

오류 응답은 `GlobalExceptionHandler` 한 곳에서 ProblemDetail(RFC 9457)로 만든다. 도메인별 `@ExceptionHandler`·`@RestControllerAdvice`를 만들지 않는다.

## 던지기

- 사용자에게 이유를 알려야 하는 실패: `DeeppleException` + 도메인 `ErrorCode`(예: `throw new DeeppleException(LikeErrorCode.LIKE_ALREADY_EXISTS, "senderId=" + senderId)`)
- 코드 버그(불가능한 상태, 잘못된 인자): `IllegalArgumentException`·`IllegalStateException` 등 Java 기본 예외. 500으로 응답된다.
- 동시성 충돌: Spring `ConcurrencyFailureException` 계열(`OptimisticLockingFailureException` 등). 409로 응답된다.
- 새 예외 클래스를 만들지 않는다. 오류는 `ErrorCode`로 구분하고, catch해서 구분할 때도 `e.getErrorCode()`로 비교한다.
- 이미 있음·없음처럼 예상된 상황은 예외를 던지고 catch하지 말고 멱등 메서드(`createIfAbsent` 등)로 처리한다.

## ErrorCode

- 도메인마다 `XxxErrorCode` enum이 `ErrorCode`를 구현한다. CQRS 도메인은 `command/domain`, 헥사고날 도메인은 `domain`에 둔다.
- enum 이름이 응답 `code`다. 클라이언트가 분기에 쓰는 API 계약이므로 이미 있는 이름은 바꾸지 않는다.
- `ErrorType`은 `google.rpc.Code`의 의미대로 고른다(예: 잘못된 입력 `INVALID_ARGUMENT`, 상태가 맞지 않음 `FAILED_PRECONDITION`, 중복 `ALREADY_EXISTS`, 권한 없음 `PERMISSION_DENIED`, 외부 연동 실패 `UNAVAILABLE`). HTTP 상태는 `GlobalExceptionHandler`가 매핑하므로 도메인에서 HTTP를 다루지 않는다.
- message는 클라이언트에 그대로 나가는 고정 문구다. ID 같은 요청별 값은 넣지 않는다.
- 다른 도메인의 공통 오류(회원 없음 등)는 그 도메인의 ErrorCode로 응답한다(예: `MemberErrorCode.MEMBER_NOT_FOUND`). 다른 도메인의 ErrorCode를 직접 import하지 않고, 그 도메인이 공개한 포트가 던지게 한다(예: `FindMemberSummaryUseCase.getMemberSummary`). 의존 방향 때문에 그 도메인을 참조할 수 없으면 같은 이름의 코드를 자기 ErrorCode에 둔다(예: `AuthErrorCode.MEMBER_NOT_FOUND`).

## detail

- `DeeppleException`의 두 번째 인자 detail은 로그에만 남고 응답에는 나가지 않는다. 원인 추적에 필요한 ID를 `key=value`로 담는다.
- 전화번호·이메일·이름·토큰·비밀번호 같은 개인정보와 secret은 detail에 넣지 않는다.
- 응답에 값을 더 내려야 하면 `withProperty(name, value)`로 ProblemDetail extension을 붙인다(예: 일시 정지 만료일 `suspensionExpireAt`).

