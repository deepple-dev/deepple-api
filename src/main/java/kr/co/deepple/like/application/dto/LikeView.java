package kr.co.deepple.like.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record LikeView(
    long likeId,
    long opponentId,
    String profileImageUrl,
    String nickname,
    @Schema(description = "지역. 값은 회원 프로필의 city와 같다", example = "SEOUL") String city,
    Integer age,
    boolean isMutualLike,
    LocalDateTime createdAt) {}
