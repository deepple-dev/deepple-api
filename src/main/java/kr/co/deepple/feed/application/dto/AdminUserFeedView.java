package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.domain.FeedType;

public record AdminUserFeedView(
    long feedId,
    @Schema(implementation = FeedType.class, description = "TEXT·POLL 중 하나") String type,
    @Schema(implementation = FeedStatus.class) String status,
    @Schema(description = "작성자 닉네임. 목록에서 타이틀 대신 쓴다") String authorNickname,
    @Schema(description = "글 피드는 내용, 투표 피드는 질문") String content,
    @Schema(description = "사진 피드만 1장, 배경 피드는 빈 목록") List<FeedImageView> images,
    LocalDateTime createdAt,
    @Schema(description = "공개된 시각. 공개된 적이 없으면 null") LocalDateTime publishedAt) {}
