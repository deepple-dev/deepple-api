package kr.co.deepple.member.command.application.member.dto;

public record MemberLoginServiceDto(
    String accessToken,
    String refreshToken,
    boolean isProfileSettingNeeded,
    String activityStatus) {}
