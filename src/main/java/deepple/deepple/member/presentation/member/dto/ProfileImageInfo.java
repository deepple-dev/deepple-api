package deepple.deepple.member.presentation.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추가 프로필 이미지 (메인 이미지 제외)")
public record ProfileImageInfo(
    @Schema(description = "이미지 URL", example = "https://cdn.deepple.co.kr/profile/2.jpg")
    String url,

    @Schema(description = "이미지 정렬 순서", example = "1")
    int order
) {
}
