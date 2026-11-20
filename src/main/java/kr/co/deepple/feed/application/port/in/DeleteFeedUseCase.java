package kr.co.deepple.feed.application.port.in;

public interface DeleteFeedUseCase {
  void deleteFeed(long memberId, long feedId);
}
