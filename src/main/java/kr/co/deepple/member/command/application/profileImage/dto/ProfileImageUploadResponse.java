package kr.co.deepple.member.command.application.profileImage.dto;

import lombok.Builder;

@Builder
public record ProfileImageUploadResponse(
    Long id, String imageUrl, Integer order, Boolean isPrimary) {}
