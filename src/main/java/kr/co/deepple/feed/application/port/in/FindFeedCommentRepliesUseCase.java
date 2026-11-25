package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.FeedCommentViews;

public interface FindFeedCommentRepliesUseCase {
  FeedCommentViews findReplies(long memberId, long commentId, String cursor);
}
