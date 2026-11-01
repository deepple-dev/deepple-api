---
paths:
  - "**/*.java"
---

# Java

들여쓰기·공백·줄 길이(120자) 같은 서식은 IntelliJ 코드 스타일(`.idea/codeStyles`)을 따른다. 아래는 포맷터가 잡지 못하는 규칙이다.

## 타입

- null이 될 수 없는 값은 primitive 타입(`long`, `int`, `boolean`, `double`)을 쓴다.
- wrapper 타입(`Long`, `Integer`, `Boolean`, `Double`)은 null이 의미 있을 때만 쓴다.
  - 저장 전에 null인 엔티티 PK(`Long id`), nullable 컬럼
  - 요청 DTO에서 값이 없을 수 있는 필드 (`@NotNull` 검증 대상 포함)
  - 제네릭 타입 인자 (`List<Long>`)
- 기존 코드의 wrapper를 고칠 때는 null 가능성을 확인한 뒤에 바꾼다.

## 이름

- 한글 발음을 영어로 옮겨 쓰지 않는다. (나쁜 예: `moohyungJasan`, 좋은 예: `intangibleAssets`) 한국어 고유명사는 예외.
- 약어도 카멜 표기법을 따른다. (나쁜 예: `HTTPAPIURL`, 좋은 예: `HttpApiUrl`)
- 메서드 이름은 동사로 시작한다. 변환 메서드(`toString`)와 빌더 메서드(`withUserId`)는 예외.
- 메서드 범위를 넘는 변수에 한 글자 이름을 쓰지 않는다. 반복문 인덱스·람다 파라미터는 허용.

## 작성

- 조건문·반복문이 한 줄이어도 중괄호를 쓴다.
- `var`는 오른쪽 식에서 타입이 바로 보일 때만 쓴다.
- 스트림·메서드 체이닝이 2단계 이상이면 메서드마다 줄을 바꾼다.
- `long` 리터럴에는 대문자 `L`을 붙인다.
