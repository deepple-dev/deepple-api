package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.FeedCommentViews;

public interface FindFeedCommentsUseCase {
  FeedCommentViews findComments(long memberId, long feedId, String cursor);
}
