package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.co.deepple.feed.domain.FeedType;

public record FeedGridItemView(
    long feedId,
    @Schema(description = "회원 피드라 TEXT·POLL만 온다", implementation = FeedType.class) String type,
    @Schema(description = "사진 또는 배경의 썸네일") String thumbnailUrl,
    @Schema(description = "글 피드는 내용, 투표 피드는 질문") String content,
    @Schema(description = "투표 피드의 선택지 내용. 투표 피드가 아니면 빈 목록") List<String> pollOptions,
    FeedTextPosition textPosition,
    long likeCount,
    long commentCount,
    @Schema(description = "신고가 쌓여 숨겨진 피드인지. 다른 사람에게는 보이지 않아 작성자에게만 true가 될 수 있다")
        boolean isHiddenByReport) {}
