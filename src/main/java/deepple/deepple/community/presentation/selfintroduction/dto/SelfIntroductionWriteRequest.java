package deepple.deepple.community.presentation.selfintroduction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SelfIntroductionWriteRequest(
    @NotBlank String title,
    @Size(min = 30, message = "내용은 최소 30자 이상이어야 합니다.") String content,
    String imageUrl
) {
}
