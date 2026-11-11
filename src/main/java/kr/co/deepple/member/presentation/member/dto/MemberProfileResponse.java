package kr.co.deepple.member.presentation.member.dto;

import kr.co.deepple.member.query.member.view.InterviewResultView;
import kr.co.deepple.member.query.member.view.IntroductionInfo;
import kr.co.deepple.member.query.member.view.MatchInfo;
import kr.co.deepple.member.query.member.view.ProfileExchangeInfo;

import java.util.List;

public record MemberProfileResponse(
    MemberInfo memberInfo,
    MatchInfo matchInfo,
    ContactInfo contactInfo,
    ProfileExchangeInfo profileExchangeInfo,
    IntroductionInfo introductionInfo,
    List<InterviewResultView> interviews
) {
}
