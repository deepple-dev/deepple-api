package kr.co.deepple.community.application.dto;

import java.time.LocalDateTime;

public record RawSelfIntroductionView(
    long id,
    long memberId,
    String title,
    String content,
    Long imageId,
    String imageUrl,
    LocalDateTime createdAt) {}
