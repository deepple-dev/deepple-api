package kr.co.deepple.feed.application.port.in;

public interface SyncFeedBlockUseCase {

  void syncBlock(long blockerId, long blockedId);
}
