package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import kr.co.deepple.feed.domain.FeedStatus;

public record AdminOfficialFeedView(
    long feedId,
    @Schema(implementation = FeedStatus.class) String status,
    @Schema(description = "타이틀. 앱에는 보이지 않는다") String title,
    String content,
    List<FeedImageView> images,
    @Schema(
            description = "보여줄 등급 목록. 비어 있으면 모든 등급에 보인다. 값은 회원의 grade와 같다",
            example = "[\"DIAMOND\"]")
        List<String> targetGrades,
    @Schema(description = "상단 고정 여부") boolean isPinned,
    LocalDateTime createdAt,
    @Schema(description = "공개된 시각. 공개된 적이 없으면 null") LocalDateTime publishedAt) {}
