package kr.co.deepple.member.presentation.introduction.dto;

import kr.co.deepple.member.query.introduction.application.MemberIntroductionProfileView;

import java.util.List;

public record MemberIntroductionProfileViews(
    List<MemberIntroductionProfileView> introductions
) {
}
