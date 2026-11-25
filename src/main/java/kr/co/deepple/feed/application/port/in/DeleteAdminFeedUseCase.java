package kr.co.deepple.feed.application.port.in;

public interface DeleteAdminFeedUseCase {

  void deleteUserFeed(long feedId);

  void deleteOfficialFeed(long feedId);
}
