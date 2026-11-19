package kr.co.deepple.admin.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DeadLetterReplayView(@Schema(description = "원래 토픽에 다시 넣은 메시지 수") long replayed) {}
