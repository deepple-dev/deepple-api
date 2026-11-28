package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record OfficialFeedGridViews(
    List<OfficialFeedGridItemView> feeds,
    @Schema(description = "다음 페이지 요청에 넣을 커서. 마지막 페이지면 null") String nextCursor,
    boolean hasMore,
    @Schema(description = "내 등급에 보이는 공개 관리자 피드 전체 개수. 등급이 없으면 0. 페이지와 상관없이 같은 값") long totalCount) {

  public static OfficialFeedGridViews empty() {
    return new OfficialFeedGridViews(List.of(), null, false, 0L);
  }
}
