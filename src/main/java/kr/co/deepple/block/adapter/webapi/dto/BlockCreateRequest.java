package kr.co.deepple.block.adapter.webapi.dto;

import jakarta.validation.constraints.NotNull;

public record BlockCreateRequest(
    @NotNull
    Long blockedId
) {
}
