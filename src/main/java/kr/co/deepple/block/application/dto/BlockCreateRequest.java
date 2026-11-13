package kr.co.deepple.block.application.dto;

import jakarta.validation.constraints.NotNull;

public record BlockCreateRequest(@NotNull Long blockedId) {}
