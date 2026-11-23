package kr.co.deepple.feed.application.port.in;

public interface LikeFeedUseCase {
  void likeFeed(long memberId, long feedId);
}
