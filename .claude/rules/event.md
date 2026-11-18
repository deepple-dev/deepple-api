---
paths:
  - "src/main/java/**/event/**/*.java"
  - "src/main/java/**/application/**/*.java"
  - "src/main/java/**/adapter/in/kafka/**/*.java"
  - "src/test/resources/contracts/**"
---

# 이벤트

- 서비스가 쓰기 트랜잭션 안에서 `ApplicationEventPublisher`로 발행한다. 읽기 전용 트랜잭션이나 트랜잭션 밖에서 발행하면 `event_publication`에 기록되지 않아 처리되지 않는다.
- 다른 모듈 이벤트를 Kafka로 받는 모듈은 토픽마다 클래스 하나에 `@KafkaListener(topics = <Module>Topic.NAME, groupId = <모듈 이름>)`을 붙이고 이벤트마다 `@KafkaHandler`를 둔다. 한 토픽에 여러 이벤트가 섞이므로 `__TypeId__` 헤더로 고르고, 클래스는 `EventConsumer`를 상속해 안 쓰는 이벤트를 버린다. 같은 이벤트를 다시 받을 수 있어(at-least-once) 멱등하게 만들고, 상태를 덮어쓰는 핸들러는 `occurredAt`이 마지막 반영 시각보다 새로울 때만 반영한다.
- 그 밖의 핸들러는 `@ApplicationModuleListener`로 듣고, 외부 호출이나 분산 락처럼 트랜잭션 밖에서 돌아야 하면 `propagation = Propagation.NOT_SUPPORTED`를 준다. 실패하면 재제출되므로 여러 번 실행돼도 결과가 같게 만든다.
- 이벤트 필드는 wrapper 타입(`Long` 등)으로 추가한다. 옛 기록에 없는 primitive 필드는 읽기에 실패한다.
- 이벤트 클래스·핸들러의 이름이나 패키지를 바꾸거나 필드를 지우기 전에 `event_publication`의 미완료 기록을 확인한다.
- Kafka로 보내는 이벤트는 클래스 이름이나 패키지를 바꾸지 않는다. `__TypeId__` 헤더에 클래스 전체 이름이 실려, 토픽에 남은 옛 메시지를 읽지 못하고 DLT로 간다. 모양이 달라지면 새 이벤트를 만든다.
- 다른 모듈이 듣는 이벤트는 `@Externalized`로 발행 모듈의 Kafka 토픽에 보내고, 키는 순서를 지켜야 하는 가장 좁은 id로 고른다. 형식을 바꾸면 `contracts`에 sample 새 버전을 추가한다.
