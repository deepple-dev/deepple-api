package deepple.deepple.member.presentation.introduction.dto;

import deepple.deepple.member.query.introduction.application.MemberIntroductionProfileView;

import java.util.List;

public record MemberIntroductionProfileViews(
    List<MemberIntroductionProfileView> introductions
) {
}
