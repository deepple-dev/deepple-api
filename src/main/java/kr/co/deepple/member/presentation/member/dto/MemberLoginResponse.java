package kr.co.deepple.member.presentation.member.dto;

public record MemberLoginResponse(
    String accessToken, boolean isProfileSettingNeeded, String activityStatus) {}
