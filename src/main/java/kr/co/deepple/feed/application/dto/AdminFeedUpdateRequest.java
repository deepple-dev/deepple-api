package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import kr.co.deepple.feed.domain.FeedStatus;

@Schema(description = "바꿀 값만 보낸다. 보내지 않은 값은 그대로 둔다")
public record AdminFeedUpdateRequest(
    @Schema(description = "타이틀. 관리자 피드만") @Size(max = 50, message = "타이틀은 50자 이하여야 합니다.")
        String title,
    @Schema(description = "내용. 관리자 피드만") @Size(max = 200, message = "내용은 200자 이하여야 합니다.")
        String content,
    @Schema(description = "이미지 ID 목록 전체. 관리자 피드만") @Size(max = 5, message = "이미지는 5장 이하여야 합니다.")
        List<Long> imageIds,
    @Schema(
            implementation = FeedStatus.class,
            description =
                "사진 피드는 PENDING에서 PUBLISHED·REJECTED로, 관리자 피드는 PUBLISHED·HIDDEN 사이로만 바꿀 수 있다")
        String status,
    @Schema(
            description = "보여줄 등급 목록. 관리자 피드만. 빈 목록이면 모든 등급에 보인다. 값은 회원의 grade와 같다",
            example = "[\"DIAMOND\"]")
        List<@NotBlank(message = "등급은 비어 있을 수 없습니다.") String> targetGrades,
    @Schema(description = "상단 고정 여부. 관리자 피드만") Boolean isPinned) {}
