---
paths:
  - "src/main/java/**/common/infra/**/*.java"
  - "src/main/java/**/adapter/**/*.java"
  - "src/main/java/**/common/config/**/*.java"
---

# 외부 API 연동 (Resilience)

외부 시스템 장애가 내부로 번지지 않게 한다(예시: `notification/adapter/out/fcm/FcmResilienceConfig.java`).

## 필수

- 외부 API 호출에는 연결·읽기 타임아웃을 반드시 설정한다.
- 재시도·서킷 브레이커 정책은 `ResiliencePolicyConfigurer`를 구현한 `@Configuration`에 정의하고, 호출 메서드에 `@Retry`, `@CircuitBreaker`로 적용한다. 정책 이름은 상수로 둔다.

## 재시도 (Retry)

- 재시도 횟수는 작게 잡는다(보통 최초 1회 + 재시도 1회). 대기 시간은 지수 백오프에 지터를 더한다.
- 예외 분류 우선순위: `ignoreExceptions` → `retryExceptions` → `retryOnException`
  - 재시도로 해결되지 않는 예외(400, 404, 잘못된 입력)는 `ignoreExceptions`
  - 일시적 오류(429, 500, 연결 실패)는 `retryExceptions`
- 조회(Query) API는 재시도해도 부작용이 적다.
- 결제·알림 발송 같은 쓰기(Command) API는 중복 처리 위험이 있다. 요청이 이미 처리됐을 수 있는 읽기 타임아웃(`SocketTimeoutException`)에는 재시도하지 않는다.

## 서킷 브레이커 (CircuitBreaker)

- 외부 시스템 장애로 볼 예외(429, 500, 연결 실패)만 `recordExceptions`로 집계한다. 클라이언트 잘못(400, 401, 404)은 `ignoreExceptions`로 둔다.
- 재시도 대상 예외와 집계 대상 예외는 다를 수 있다. 재시도는 "다시 하면 되는가", 집계는 "외부 장애인가"로 판단한다.
- 트래픽이 적으면 `minimumNumberOfCalls`를 너무 크게 잡지 않는다.

## Fallback

- `@CircuitBreaker`의 fallback이 `@Retry`의 fallback보다 먼저 동작한다. CircuitBreaker fallback이 예외를 던지면 Retry fallback이 동작하므로, 둘 다 정의할 때는 이 순서를 고려한다.
- fallback에서는 도메인 예외로 감싸서 던지거나 캐시된 값을 돌려준다.
