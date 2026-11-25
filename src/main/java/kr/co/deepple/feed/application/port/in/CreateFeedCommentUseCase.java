package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.FeedCommentCreateRequest;
import kr.co.deepple.feed.application.dto.FeedCommentView;

public interface CreateFeedCommentUseCase {
  FeedCommentView createComment(long memberId, long feedId, FeedCommentCreateRequest request);
}
