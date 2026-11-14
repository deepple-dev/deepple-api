package kr.co.deepple.like.query;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.co.deepple.member.domain.City;

public record LikeView(
    long likeId,
    long opponentId,
    String profileImageUrl,
    String nickname,
    @Schema(implementation = City.class) String city,
    int age,
    boolean isMutualLike,
    LocalDateTime createdAt) {}
