---
paths:
  - "**/*.java"
---

# Java

아래는 포맷터가 잡지 못하는 규칙이다.

## 타입

- null이 될 수 없는 값은 primitive 타입(`long`, `int`, `boolean`, `double`)을 쓴다.
- wrapper 타입(`Long`, `Integer`, `Boolean`, `Double`)은 null이 의미 있을 때만 쓴다.
  - 저장 전에 null인 엔티티 PK(`Long id`), nullable 컬럼
  - 요청 DTO에서 값이 없을 수 있는 필드 (`@NotNull` 검증 대상 포함)
  - 제네릭 타입 인자 (`List<Long>`)
- 기존 코드의 wrapper를 고칠 때는 null 가능성을 확인한 뒤에 바꾼다.

## 작성

- 조건문·반복문이 한 줄이어도 중괄호를 쓴다.
- `var`는 오른쪽 식에서 타입이 바로 보일 때만 쓴다.
- `long` 리터럴에는 대문자 `L`을 붙인다.
- 생성자·getter·로거처럼 Lombok으로 대신할 수 있는 코드는 직접 쓰지 않고 Lombok을 쓴다. 테스트 코드도 같다. 예를 들어 유틸 클래스의 private 생성자는 `@NoArgsConstructor(access = AccessLevel.PRIVATE)`로 만든다.
