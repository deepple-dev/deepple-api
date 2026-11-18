---
paths:
  - "src/main/java/**/application/**/*.java"
  - "src/main/java/**/adapter/out/persistence/**/*.java"
---

# 동시성

- 이미 있는 행의 값 변경은 비관적 락(`@Lock(PESSIMISTIC_WRITE)`, 예: `MemberRepository.findByIdForUpdate`)으로 막는다. 락 대기는 3초(`innodb_lock_wait_timeout`)다.
- 아직 없는 행의 중복 생성은 unique 제약으로 막는다. 제약으로 표현할 수 없는 규칙이거나 저장 전 작업까지 줄 세워야 하면 `DistributedLockTemplate.executeInTransaction`(Redisson)을 함께 쓴다.
- 분산 락을 잡는 메서드에는 `@Transactional`을 붙이지 않는다(락 → 트랜잭션 → 커밋 → 해제 순서).
