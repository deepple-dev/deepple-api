package deepple.deepple.community.presentation.selfintroduction.dto;

import deepple.deepple.community.query.selfintroduction.view.SelfIntroductionSummaryView;

import java.util.List;

public record SelfIntroductionSummaryViews(
    List<SelfIntroductionSummaryView> selfIntroductions
) {
}
