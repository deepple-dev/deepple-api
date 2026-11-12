package kr.co.deepple.like.presentation;

import java.util.List;
import kr.co.deepple.like.query.LikeView;

public record LikeViews(List<LikeView> likes, boolean hasMore) {}
