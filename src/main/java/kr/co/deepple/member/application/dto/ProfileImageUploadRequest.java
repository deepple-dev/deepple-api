package kr.co.deepple.member.application.dto;

import jakarta.validation.constraints.NotNull;

public record ProfileImageUploadRequest(@NotNull(message = "이미지 ID를 입력해주세요") Long imageId) {}
