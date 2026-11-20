package kr.co.deepple.member.application.dto;

public record ProfileImageView(Long id, Long imageId, String url, boolean isPrimary, int order) {}
