package kr.co.deepple.feed.application.port.in;

public interface DeleteFeedCommentUseCase {
  void deleteComment(long memberId, long commentId);
}
