package kr.co.deepple.like.application.dto;

import java.util.List;

public record LikeViews(List<LikeView> likes, boolean hasMore) {}
