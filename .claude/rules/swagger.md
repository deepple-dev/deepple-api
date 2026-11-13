---
paths:
  - "src/main/java/**/presentation/**/*.java"
  - "src/main/java/**/adapter/in/web/**/*.java"
  - "src/main/java/**/application/dto/**/*.java"
  - "src/main/java/**/query/**/*.java"
---

# Swagger 문서화

FE가 Swagger UI만 보고 API를 쓸 수 있게 하고, enum 허용값이 문서와 어긋나지 않게 한다.

## 컨트롤러

- 클래스에 `@Tag(name = "...")`, 메서드에 `@Operation(summary = "...")`를 붙인다.
- 이름과 요약에는 Figma(화면 기획)에서 쓰는 용어를 쓴다(예: `@Tag(name = "어드민 인터뷰 질문 관리 API")`, `@Operation(summary = "어드민 인터뷰 질문 생성 API")`).

## enum을 문자열로 주고받는 필드

요청·응답·검색 조건 record에서 enum을 `String`으로 다루면 `@Schema(implementation = Enum클래스.class)`로 허용값을 노출한다.

```java
public record InterviewQuestionSaveRequest(
    @Schema(implementation = InterviewCategory.class)
    @NotBlank(message = "인터뷰 질문 카테고리는 필수입니다.")
    String category
) {
}
```

## 검색 조건 (Query Parameter)

- 검색 조건은 `XxxSearchCondition` record로 받고, 컨트롤러 파라미터에 `@Valid @ModelAttribute`를 붙인다.
- 파라미터 변수명은 클래스명과 같게 짓는다(`interviewQuestionSearchCondition`). Swagger UI 하단 Schemas에서 같은 이름으로 찾을 수 있게 하기 위해서다.
