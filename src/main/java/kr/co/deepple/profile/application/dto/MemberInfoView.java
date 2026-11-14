package kr.co.deepple.profile.application.dto;

import java.util.Set;

public record MemberInfoView(
    StatusInfo statusInfo,
    BasicInfo basicInfo,
    ProfileInfo profileInfo,
    Set<InterviewInfoView> interviewInfoView) {}
