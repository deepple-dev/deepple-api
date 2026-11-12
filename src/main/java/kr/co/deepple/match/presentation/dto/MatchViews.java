package kr.co.deepple.match.presentation.dto;

import java.util.List;
import kr.co.deepple.match.query.MatchView;

public record MatchViews(List<MatchView> matches, boolean hasMore) {}
