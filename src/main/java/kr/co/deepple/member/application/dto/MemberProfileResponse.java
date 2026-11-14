package kr.co.deepple.member.application.dto;

import java.util.List;

public record MemberProfileResponse(
    MemberInfo memberInfo,
    MatchInfo matchInfo,
    ContactInfo contactInfo,
    ProfileExchangeInfo profileExchangeInfo,
    IntroductionInfo introductionInfo,
    List<InterviewResultView> interviews) {}
