package kr.co.deepple.member.presentation.introduction.dto;

import java.util.List;
import kr.co.deepple.member.query.introduction.application.MemberIntroductionProfileView;

public record MemberIntroductionProfileViews(List<MemberIntroductionProfileView> introductions) {}
