package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.domain.FeedType;

public record AdminFeedView(
    long feedId,
    @Schema(implementation = FeedType.class) String type,
    @Schema(implementation = FeedStatus.class) String status,
    @Schema(description = "타이틀. 사용자 피드는 null") String title,
    @Schema(description = "작성자 닉네임. 관리자 피드는 공식 계정 이름") String authorNickname,
    String content,
    List<FeedImageView> images,
    @Schema(description = "상단 고정 여부") boolean isPinned,
    LocalDateTime createdAt,
    @Schema(description = "공개된 시각. 공개된 적이 없으면 null") LocalDateTime publishedAt) {}
