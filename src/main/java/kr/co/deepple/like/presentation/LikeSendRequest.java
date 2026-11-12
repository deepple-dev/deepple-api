package kr.co.deepple.like.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.co.deepple.like.command.domain.LikeLevel;

public record LikeSendRequest(
    long receiverId,
    @Schema(implementation = LikeLevel.class) @NotNull LikeLevelRequest likeLevel) {}
