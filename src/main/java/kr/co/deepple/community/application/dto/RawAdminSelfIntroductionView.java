package kr.co.deepple.community.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record RawAdminSelfIntroductionView(
    long id,
    long memberId,
    boolean isOpened,
    String content,
    String imageUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt) {

  @QueryProjection
  public RawAdminSelfIntroductionView {}
}
