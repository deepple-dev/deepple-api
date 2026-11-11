package kr.co.deepple.community.presentation.selfintroduction.dto;

public record SelfIntroductionSummaryResponse(
    Long id,
    String name,
    String profileUrl,
    Integer age,
    String title
) {
}
