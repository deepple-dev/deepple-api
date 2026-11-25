package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.domain.FeedType;

public record AdminUserFeedDetailView(
    long feedId,
    @Schema(implementation = FeedType.class, description = "TEXT·POLL 중 하나") String type,
    @Schema(implementation = FeedStatus.class) String status,
    long authorMemberId,
    String authorNickname,
    @Schema(description = "글 피드는 내용, 투표 피드는 질문") String content,
    @Schema(description = "투표 선택지. 화면 순서대로이고, 글 피드는 빈 목록") List<String> pollOptions,
    @Schema(description = "사진 피드만 1장, 배경 피드는 빈 목록") List<FeedImageView> images,
    @Schema(description = "배경 피드의 배경. 사진 피드이거나 배경이 없어졌으면 null") FeedBackgroundView background,
    LocalDateTime createdAt,
    @Schema(description = "공개된 시각. 공개된 적이 없으면 null") LocalDateTime publishedAt,
    @Schema(description = "지운 시각. 지우지 않았으면 null") LocalDateTime deletedAt) {}
