package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record FeedCreateRequest(
    @Schema(
            description = "피드 종류",
            allowableValues = {"TEXT", "POLL"})
        @NotBlank(message = "피드 종류는 필수입니다.")
        String type,
    @Schema(description = "글 피드는 내용(최대 100자, 줄바꿈 허용), 투표 피드는 질문(최대 20자, 줄바꿈 불가)")
        @NotBlank(message = "피드 내용은 필수입니다.")
        @Size(max = 100, message = "피드 내용은 100자 이하여야 합니다.")
        String content,
    @NotNull(message = "글 위치는 필수입니다.") @Valid FeedTextPosition textPosition,
    @Schema(description = "배경 ID. 배경과 사진 중 하나만 보낸다") Long backgroundId,
    @Schema(description = "POST /images로 받은 이미지 ID. 배경과 사진 중 하나만 보낸다") Long imageId,
    @Schema(description = "투표 선택지. 투표 피드만 보낸다(2~4개, 각 최대 20자, 줄바꿈 불가)")
        @Size(min = 2, max = 4, message = "투표 선택지는 2개 이상 4개 이하여야 합니다.")
        List<
                @NotBlank(message = "투표 선택지는 비어 있을 수 없습니다.")
                @Size(max = 20, message = "투표 선택지는 20자 이하여야 합니다.") String>
            pollOptions) {}
