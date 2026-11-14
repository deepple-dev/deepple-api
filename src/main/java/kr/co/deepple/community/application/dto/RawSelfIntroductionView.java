package kr.co.deepple.community.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record RawSelfIntroductionView(
    long id,
    long memberId,
    String title,
    String content,
    String imageUrl,
    LocalDateTime createdAt) {

  @QueryProjection
  public RawSelfIntroductionView {}
}
