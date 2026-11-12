package kr.co.deepple.like.query;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record RawLikeView(
    long likeId,
    long opponentId,
    String profileImageUrl,
    String nickname,
    String city,
    int yearOfBirth,
    boolean isMutualLike,
    LocalDateTime createdAt) {
  @QueryProjection
  public RawLikeView {}
}
