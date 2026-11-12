package kr.co.deepple.block.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

public record BlockCreateRequest(
    @NotNull
    Long blockedId
) {
}
