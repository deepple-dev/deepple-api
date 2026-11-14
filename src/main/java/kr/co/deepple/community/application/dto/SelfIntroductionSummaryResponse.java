package kr.co.deepple.community.application.dto;

public record SelfIntroductionSummaryResponse(
    Long id, String name, String profileUrl, Integer age, String title) {}
