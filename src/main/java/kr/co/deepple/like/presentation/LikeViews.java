package kr.co.deepple.like.presentation;

import kr.co.deepple.like.query.LikeView;

import java.util.List;

public record LikeViews(List<LikeView> likes, boolean hasMore) {
}
