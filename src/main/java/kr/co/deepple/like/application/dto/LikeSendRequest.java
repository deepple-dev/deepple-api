package kr.co.deepple.like.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.co.deepple.like.domain.LikeLevel;

public record LikeSendRequest(
    long receiverId,
    @Schema(implementation = LikeLevel.class) @NotNull LikeLevelRequest likeLevel) {}
