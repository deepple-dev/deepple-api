---
paths:
  - "src/main/java/**/application/**/*.java"
  - "src/main/java/**/adapter/**/*.java"
---

# Repository

- 서비스는 저장소를 `application/port/out`의 interface로만 주입받는다. `adapter/out/persistence`의 클래스를 직접 주입받지 않는다.
- JPA 저장소는 `port/out`의 interface가 `JpaRepository`를 상속하고, 구현 클래스를 따로 두지 않는다.
- QueryDSL·Redis처럼 직접 구현하는 저장소는 `port/out`에 상속 없는 interface를 두고, `adapter/out/persistence`에 `interface 이름 + Impl` 클래스로 구현한다.
- 이름은 엔티티 저장소를 `XxxRepository`, 화면용 조회를 `XxxQueryRepository`로 짓는다. `Command`는 붙이지 않는다.
- interface 이름에는 `Redis`, `Jpa` 같은 기술 이름을 넣지 않는다. 역할을 드러내야 하면 `Cache`처럼 역할 이름을 쓴다(예: `IntroductionCacheRepository`).
- unique 위반을 도메인 예외로 바꾸거나 여러 쿼리를 묶는 로직은 서비스에 둔다.
- `@Modifying` 쿼리를 트랜잭션 밖에서 부르면 interface 메서드에 `@Transactional`을 붙인다.
