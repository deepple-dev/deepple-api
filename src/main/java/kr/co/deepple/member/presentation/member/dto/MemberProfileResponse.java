package kr.co.deepple.member.presentation.member.dto;

import java.util.List;
import kr.co.deepple.member.query.member.view.InterviewResultView;
import kr.co.deepple.member.query.member.view.IntroductionInfo;
import kr.co.deepple.member.query.member.view.MatchInfo;
import kr.co.deepple.member.query.member.view.ProfileExchangeInfo;

public record MemberProfileResponse(
    MemberInfo memberInfo,
    MatchInfo matchInfo,
    ContactInfo contactInfo,
    ProfileExchangeInfo profileExchangeInfo,
    IntroductionInfo introductionInfo,
    List<InterviewResultView> interviews) {}
