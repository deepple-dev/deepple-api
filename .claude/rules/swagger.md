---
paths:
  - "src/main/java/**/adapter/in/web/**/*.java"
  - "src/main/java/**/application/dto/**/*.java"
---

# Swagger 문서화

FE가 Swagger UI만 보고 API를 쓸 수 있게 하고, enum 허용값이 문서와 어긋나지 않게 한다.

## 컨트롤러

- 클래스에 `@Tag(name = ApiTags.도메인)`, 메서드에 `@Operation(summary = "...")`를 붙인다.
- 태그는 도메인당 하나다. 이름과 설명은 `common/config/ApiTags`에 두고, 컨트롤러마다 새 태그를 만들지 않는다. 도메인을 추가하면 `ApiTags`에 상수와 설명을 함께 추가한다.
- 문서는 앱과 관리자 두 개로 나뉜다(`SwaggerConfig`). 경로가 `/admin/**`이면 관리자 문서에, 나머지는 앱 문서에 들어간다. 관리자 API의 요약에는 "어드민"·"관리자 페이지" 같은 접두어를 붙이지 않는다.
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

다른 도메인의 enum 값을 그대로 전달하는 필드는 그 enum을 import하지 않고 `@Schema(description = "...", example = "...")`로 적는다(예: `LikeView.city`).

## 검색 조건 (Query Parameter)

- 검색 조건은 `XxxSearchCondition` record로 받고, 컨트롤러 파라미터에 `@Valid @ModelAttribute`를 붙인다.
- 파라미터 변수명은 클래스명과 같게 짓는다(`interviewQuestionSearchCondition`). Swagger UI 하단 Schemas에서 같은 이름으로 찾을 수 있게 하기 위해서다.
