package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FeedPollView(
    List<FeedPollOptionView> options,
    @Schema(description = "내가 고른 선택지 ID. 투표하지 않았으면 null") Long myOptionId,
    @Schema(description = "결과를 볼 수 있는지. 투표한 회원과 작성자만 true") boolean isResultVisible) {}
