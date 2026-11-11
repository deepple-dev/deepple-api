package kr.co.deepple.match.presentation.dto;

import kr.co.deepple.match.query.MatchView;

import java.util.List;

public record MatchViews(List<MatchView> matches, boolean hasMore) {
}
