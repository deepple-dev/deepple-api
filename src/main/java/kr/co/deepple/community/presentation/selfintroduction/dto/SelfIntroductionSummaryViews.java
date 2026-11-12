package kr.co.deepple.community.presentation.selfintroduction.dto;

import java.util.List;
import kr.co.deepple.community.query.selfintroduction.view.SelfIntroductionSummaryView;

public record SelfIntroductionSummaryViews(List<SelfIntroductionSummaryView> selfIntroductions) {}
