package kr.co.deepple.community.presentation.selfintroduction.dto;

import kr.co.deepple.community.query.selfintroduction.view.SelfIntroductionSummaryView;

import java.util.List;

public record SelfIntroductionSummaryViews(
    List<SelfIntroductionSummaryView> selfIntroductions
) {
}
