---
paths:
  - "src/main/java/**/event/**/*.java"
  - "src/main/java/**/application/**/*.java"
  - "src/test/resources/contracts/**"
---

# 이벤트

- 서비스가 쓰기 트랜잭션 안에서 `ApplicationEventPublisher`로 발행한다. 읽기 전용 트랜잭션이나 트랜잭션 밖에서 발행하면 `event_publication`에 기록되지 않아 처리되지 않는다.
- 핸들러는 `@ApplicationModuleListener`로 듣고, 외부 호출이나 분산 락처럼 트랜잭션 밖에서 돌아야 하면 `propagation = Propagation.NOT_SUPPORTED`를 준다. 실패하면 재제출되므로 여러 번 실행돼도 결과가 같게 만든다.
- 이벤트 필드는 wrapper 타입(`Long` 등)으로 추가한다. 옛 기록에 없는 primitive 필드는 읽기에 실패한다.
- 이벤트 클래스·핸들러의 이름이나 패키지를 바꾸거나 필드를 지우기 전에 `event_publication`의 미완료 기록을 확인한다.
- 다른 모듈이 듣는 이벤트는 `@Externalized`로 발행 모듈의 Kafka 토픽에 보내고, 키는 순서를 지켜야 하는 가장 좁은 id로 고른다. 형식을 바꾸면 `contracts`에 sample 새 버전을 추가한다.
