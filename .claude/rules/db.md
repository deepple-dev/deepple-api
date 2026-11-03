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
- 배포 중에는 옛 버전과 새 버전 앱이 같은 DB를 함께 쓴다(prod는 task를 하나씩 교체). 마이그레이션은 바로 이전 버전 코드가 계속 동작하도록 expand/contract로 쓴다.
  - 한 번에 해도 되는 변경: 테이블·인덱스 추가, NULL 허용 또는 기본값 있는 컬럼 추가
  - 컬럼·테이블 이름은 바꾸지 않는다. 이름이 바뀌어야 하면 엔티티 필드명만 바꾸고 `@Column(name = "기존_이름")`으로 둔다.
  - 삭제와 NOT NULL·타입 변경은 코드에서 먼저 쓰지 않게 배포한 뒤, 다음 배포 이후의 마이그레이션에서 한다.
