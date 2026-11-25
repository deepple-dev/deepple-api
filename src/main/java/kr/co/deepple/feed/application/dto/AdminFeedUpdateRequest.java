package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "바꿀 값만 보낸다. 보내지 않은 값은 그대로 둔다")
public record AdminFeedUpdateRequest(
    @Schema(description = "타이틀") @Size(max = 50, message = "타이틀은 50자 이하여야 합니다.") String title,
    @Schema(description = "내용") @Size(max = 200, message = "내용은 200자 이하여야 합니다.") String content,
    @Schema(description = "이미지 ID 목록 전체. 보낸 순서대로 저장한다")
        @Size(min = 1, max = 5, message = "이미지는 1장 이상 5장 이하여야 합니다.")
        List<@NotNull(message = "이미지 ID는 비어 있을 수 없습니다.") Long> imageIds,
    @Schema(
            description = "보여줄 등급 목록. 빈 목록이면 모든 등급에 보인다. 값은 회원의 grade와 같다",
            example = "[\"DIAMOND\"]")
        List<@NotBlank(message = "등급은 비어 있을 수 없습니다.") String> targetGrades,
    @Schema(description = "상단 고정 여부") Boolean isPinned) {}
