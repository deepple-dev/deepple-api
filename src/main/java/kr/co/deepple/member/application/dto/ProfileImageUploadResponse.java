package kr.co.deepple.member.application.dto;

import lombok.Builder;

@Builder
public record ProfileImageUploadResponse(
    Long id, Long imageId, String imageUrl, Integer order, Boolean isPrimary) {}
