package kr.co.deepple.match.application.dto;

import java.util.List;

public record MatchViews(List<MatchView> matches, boolean hasMore) {}
