---
paths:
  - "src/main/resources/db/**"
  - "src/test/resources/db/**"
  - "src/main/java/**/domain/**/*.java"
---

# DB

- 스키마 변경은 Flyway 마이그레이션(`src/main/resources/db/migration/V{다음번호}__설명.sql`)으로만 한다.
- 이미 머지된 마이그레이션 파일은 수정하지 않는다. 새 파일을 추가한다.
- FK 제약조건은 만들지 않는다.
- PK와 `*_id` 컬럼은 `bigint`로 만든다.
- local·dev·prod·test 모두 Flyway + `ddl-auto: validate`라서 엔티티와 마이그레이션이 일치해야 한다. 테스트와 로컬 실행에서 바로 확인된다.
- 테스트에만 있는 엔티티의 테이블은 `src/test/resources/db/test-migration/R__설명.sql`(반복 마이그레이션)로 만든다. main 마이그레이션 뒤에 실행되고 버전 번호가 겹치지 않는다. 다시 실행돼도 안전하게 `create table if not exists`를 쓴다.
