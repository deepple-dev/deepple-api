package kr.co.deepple.community.application.dto;

import java.time.LocalDateTime;

public record RawAdminSelfIntroductionView(
    long id,
    long memberId,
    boolean isOpened,
    String content,
    String imageUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt) {}
