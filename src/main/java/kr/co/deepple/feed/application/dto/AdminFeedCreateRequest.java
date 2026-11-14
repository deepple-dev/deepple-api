package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AdminFeedCreateRequest(
    @Schema(description = "타이틀. 앱에는 보이지 않는다")
        @NotBlank(message = "타이틀은 필수입니다.")
        @Size(max = 50, message = "타이틀은 50자 이하여야 합니다.")
        String title,
    @NotBlank(message = "내용은 필수입니다.") @Size(max = 200, message = "내용은 200자 이하여야 합니다.")
        String content,
    @Schema(description = "POST /admin/images로 받은 이미지 ID 목록")
        @Size(max = 5, message = "이미지는 5장 이하여야 합니다.")
        List<Long> imageIds,
    @NotNull(message = "공개 여부는 필수입니다.") Boolean isPublished,
    @NotNull(message = "상단 고정 여부는 필수입니다.") Boolean isPinned) {}
