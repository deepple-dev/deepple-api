package kr.co.deepple.member.application.dto;

import jakarta.validation.constraints.NotEmpty;

public record ProfileImageUploadRequest(@NotEmpty(message = "이미지 URL을 입력해주세요") String imageUrl) {}
