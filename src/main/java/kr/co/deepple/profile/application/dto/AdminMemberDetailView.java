package kr.co.deepple.profile.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminMemberDetailView(
    AdminMemberSettingInfo settingInfo,
    AdminMemberStatusInfo statusInfo,
    BasicInfo basicInfo,
    HeartBalanceView heartBalanceInfo,
    List<String> profileImageUrls,
    ProfileInfo profileInfo,
    List<InterviewInfoView> interviewInfos,
    LocalDateTime createdAt,
    LocalDateTime deletedAt) {}
